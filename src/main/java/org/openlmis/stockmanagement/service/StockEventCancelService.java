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

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_REASON_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_REASON_REQUIRED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_EVENT_NOT_FOUND;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventCancelDto;
import org.openlmis.stockmanagement.dto.StockEventCancelLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Service;

/**
 * Builds and persists the adjustment stock event that cancels selected issue/receive line items.
 * The original event is left untouched; a cancelled issue is reversed with a credit and a cancelled
 * receive with a debit. Processing is delegated to {@link StockEventProcessor} so the cancellation
 * reuses the standard validation, persistence and stock-on-hand recalculation.
 */
@Service
@RequiredArgsConstructor
public class StockEventCancelService {

  private final StockEventsRepository stockEventsRepository;
  private final StockEventCancelValidationService cancelValidationService;
  private final StockCardLineItemReasonRepository reasonRepository;
  private final StockEventProcessor stockEventProcessor;
  private final PermissionService permissionService;

  /**
   * Cancels the selected line items of the given event.
   *
   * @param eventId the original stock event id.
   * @param request the line items to cancel, their cancellation reasons and the signature.
   * @return the id of the created cancellation stock event.
   */
  public UUID cancel(UUID eventId, StockEventCancelDto request) {
    StockEvent event = stockEventsRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException(
            new Message(ERROR_STOCK_EVENT_NOT_FOUND, eventId)));

    permissionService.canCancelStockEvent(event.getProgramId(), event.getFacilityId());

    Map<UUID, StockEventCancelLineItemDto> requestByLineId = request.getLineItems().stream()
        .collect(toMap(StockEventCancelLineItemDto::getStockEventLineItemId, identity()));

    cancelValidationService.validate(event, requestByLineId.keySet());

    StockEventDto cancellation =
        buildCancellationEvent(event, request.getSignature(), requestByLineId);
    return stockEventProcessor.process(cancellation);
  }

  private StockEventDto buildCancellationEvent(StockEvent event, String signature,
      Map<UUID, StockEventCancelLineItemDto> requestByLineId) {
    Map<UUID, StockEventLineItem> originalById = event.getLineItems().stream()
        .collect(toMap(StockEventLineItem::getId, identity()));
    Map<UUID, StockCardLineItemReason> reasonsById = loadReasons(requestByLineId.values());

    List<StockEventLineItemDto> lineItems = new ArrayList<>();
    for (StockEventCancelLineItemDto requested : requestByLineId.values()) {
      StockEventLineItem original = originalById.get(requested.getStockEventLineItemId());
      StockCardLineItemReason reason = resolveCancelReason(requested, original, reasonsById);
      lineItems.add(StockEventLineItemDto.builder()
          .orderableId(original.getOrderableId())
          .lotId(original.getLotId())
          .quantity(original.getQuantity())
          .occurredDate(LocalDate.now())
          .reasonId(reason.getId())
          .reasonFreeText(requested.getReasonFreeText())
          .reversesEventLineItemId(original.getId())
          .build());
    }

    StockEventDto cancellation = new StockEventDto();
    cancellation.setFacilityId(event.getFacilityId());
    cancellation.setProgramId(event.getProgramId());
    cancellation.setSignature(signature);
    cancellation.setActive(true);
    cancellation.setLineItems(lineItems);
    return cancellation;
  }

  private Map<UUID, StockCardLineItemReason> loadReasons(
      Collection<StockEventCancelLineItemDto> requested) {
    Set<UUID> reasonIds = requested.stream()
        .map(StockEventCancelLineItemDto::getReasonId)
        .filter(Objects::nonNull)
        .collect(toSet());
    if (reasonIds.isEmpty()) {
      return emptyMap();
    }
    return reasonRepository.findByIdIn(reasonIds).stream()
        .collect(toMap(StockCardLineItemReason::getId, identity()));
  }

  private StockCardLineItemReason resolveCancelReason(StockEventCancelLineItemDto requested,
      StockEventLineItem original, Map<UUID, StockCardLineItemReason> reasonsById) {
    UUID reasonId = requested.getReasonId();
    if (reasonId == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_CANCELLATION_REASON_REQUIRED, original.getId()));
    }
    StockCardLineItemReason reason = reasonsById.get(reasonId);
    if (reason == null
        || !reason.isAdjustmentReasonCategory()
        || !reason.getTags().contains(StockEventCancelValidationService.CANCEL_TAG)
        || !countersMovement(original, reason)) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_CANCELLATION_REASON_INVALID, reasonId));
    }
    return reason;
  }

  private boolean countersMovement(StockEventLineItem original, StockCardLineItemReason reason) {
    // an issue (has a destination) is reversed with a credit; a receive (has a source) with a debit
    return original.isIssue()
        ? reason.isCreditReasonType()
        : reason.isDebitReasonType();
  }
}
