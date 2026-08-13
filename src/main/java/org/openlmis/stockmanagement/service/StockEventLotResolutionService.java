/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.stockmanagement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemLotDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Resolves stock event line items that address a lot by code and expiry (instead of a lot id) to a
 * concrete lot id, creating the lot in the reference data service when the event shape permits it.
 * Runs before the event is validated and persisted so downstream processing sees a resolved lot id.
 */
@Service
@RequiredArgsConstructor
public class StockEventLotResolutionService {

  private static final String TRADE_ITEM = "tradeItem";

  private final OrderableReferenceDataService orderableReferenceDataService;

  private final LotReferenceDataService lotReferenceDataService;

  private final LotCodeValidator lotCodeValidator;

  /**
   * Resolves every line item that carries a lot code and expiry. The (tradeItem, lotCode) pair is
   * matched against existing lots; a match is reused, otherwise the lot is created when the event
   * shape permits it. The resolved id is written back onto the line's {@code lotId}.
   *
   * @param eventDto the stock event being processed
   */
  public void resolve(StockEventDto eventDto) {
    List<StockEventLineItemDto> lineItems = eventDto.getLineItems();
    if (CollectionUtils.isEmpty(lineItems)) {
      return;
    }

    List<StockEventLineItemDto> codeAddressed = lineItems.stream()
        .filter(StockEventLineItemDto::hasLot)
        .collect(Collectors.toList());
    if (codeAddressed.isEmpty()) {
      return;
    }

    Map<UUID, UUID> tradeItemByOrderable = mapOrderablesToTradeItems(codeAddressed);
    // lots fetched once per trade item and reused across lines that share it
    Map<UUID, List<LotDto>> lotsByTradeItem = new HashMap<>();

    for (StockEventLineItemDto lineItem : codeAddressed) {
      lineItem.setLotId(resolveLine(lineItem, tradeItemByOrderable, lotsByTradeItem));
    }
  }

  private UUID resolveLine(StockEventLineItemDto lineItem, Map<UUID, UUID> tradeItemByOrderable,
      Map<UUID, List<LotDto>> lotsByTradeItem) {
    StockEventLineItemLotDto lot = lineItem.getLot();

    if (lineItem.hasLotId()) {
      throw new ValidationMessageException(
          new Message(MessageKeys.ERROR_EVENT_LOT_ID_AND_CODE_EXCLUSIVE, lot.getLotCode()));
    }
    lotCodeValidator.validate(lot.getLotCode());

    UUID tradeItemId = tradeItemByOrderable.get(lineItem.getOrderableId());
    if (tradeItemId == null) {
      throw new ValidationMessageException(new Message(
          MessageKeys.ERROR_EVENT_LOT_ORDERABLE_WITHOUT_TRADE_ITEM,
          lot.getLotCode(), lineItem.getOrderableId()));
    }

    return resolveOrCreate(tradeItemId, lot, isCreationAllowed(lineItem), lotsByTradeItem);
  }

  private UUID resolveOrCreate(UUID tradeItemId, StockEventLineItemLotDto lot,
      boolean creationAllowed, Map<UUID, List<LotDto>> lotsByTradeItem) {
    List<LotDto> lots = lotsByTradeItem.computeIfAbsent(
        tradeItemId, lotReferenceDataService::getAllLotsOf);
    Optional<LotDto> existing = findByCode(lots, lot.getLotCode());
    if (existing.isPresent()) {
      return existing.get().getId();
    }
    if (!creationAllowed) {
      throw new ValidationMessageException(
          new Message(MessageKeys.ERROR_EVENT_LOT_CREATION_NOT_ALLOWED, lot.getLotCode()));
    }
    try {
      return lotReferenceDataService.create(buildLot(tradeItemId, lot)).getId();
    } catch (ValidationMessageException ex) {
      // A concurrent create of the same code conflicts in reference data; re-read fresh
      // (case-insensitive), bypassing the cache, and reuse the existing lot instead of failing.
      return findByCode(lotReferenceDataService.getAllLotsOf(tradeItemId), lot.getLotCode())
          .map(LotDto::getId)
          .orElseThrow(() -> ex);
    }
  }

  private Optional<LotDto> findByCode(List<LotDto> lots, String lotCode) {
    return lots.stream()
        .filter(lot -> lotCode.equalsIgnoreCase(lot.getLotCode()))
        .findFirst();
  }

  private LotDto buildLot(UUID tradeItemId, StockEventLineItemLotDto lot) {
    return LotDto.builder()
        .lotCode(lot.getLotCode())
        .expirationDate(lot.getExpirationDate())
        .tradeItemId(tradeItemId)
        .active(true)
        .build();
  }

  /**
   * A lot may be created only for stock movements that legitimately introduce new lots: receiving
   * (a credit carrying a source) and physical inventory (no source, destination, or reason). Issues
   * and adjustments must reference existing lots. Derived from the line's shape; the per-screen
   * confirmation nuance is a client concern and is not verifiable server-side.
   */
  private boolean isCreationAllowed(StockEventLineItemDto lineItem) {
    return !lineItem.hasDestinationId()
        && (lineItem.hasSourceId() || !lineItem.hasReasonId());
  }

  private Map<UUID, UUID> mapOrderablesToTradeItems(List<StockEventLineItemDto> lineItems) {
    Set<UUID> orderableIds = lineItems.stream()
        .map(StockEventLineItemDto::getOrderableId)
        .collect(Collectors.toSet());

    Map<UUID, UUID> tradeItemByOrderable = new HashMap<>();
    for (OrderableDto orderable : orderableReferenceDataService.findByIds(orderableIds)) {
      String tradeItemId = orderable.getIdentifiers() == null
          ? null
          : orderable.getIdentifiers().get(TRADE_ITEM);
      if (tradeItemId != null) {
        tradeItemByOrderable.put(orderable.getId(), UUID.fromString(tradeItemId));
      }
    }
    return tradeItemByOrderable;
  }
}
