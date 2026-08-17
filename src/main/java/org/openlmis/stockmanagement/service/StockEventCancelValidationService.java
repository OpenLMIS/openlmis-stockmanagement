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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_VALIDATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_IS_CANCELLATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_NO_LINE_ITEMS;

import java.time.ZonedDateTime;
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
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.BlockingTransactionDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Service;

/**
 * Validates that selected line items of an issue/receive stock event may be cancelled. Every
 * per-line failure is collected so the caller learns about all blocking line items at once.
 */
@Service
@RequiredArgsConstructor
public class StockEventCancelValidationService {

  static final String CANCEL_TAG = "cancel";
  static final String PHYSICAL_INVENTORY_TYPE = "PHYSICAL_INVENTORY";

  private final StockEventLineItemRepository stockEventLineItemRepository;
  private final PhysicalInventoriesRepository physicalInventoriesRepository;
  private final StockCardLineItemReasonRepository reasonRepository;

  /**
   * Validates that the selected line items of the given event can be cancelled. Collects every
   * per-line failure and, if any, throws a single {@link StockEventCancellationException}.
   *
   * @param event               the original stock event being cancelled.
   * @param lineItemIdsToCancel ids of the event's line items selected for cancellation.
   */
  public void validate(StockEvent event, Collection<UUID> lineItemIdsToCancel) {
    if (lineItemIdsToCancel == null || lineItemIdsToCancel.isEmpty()) {
      throw new ValidationMessageException(new Message(ERROR_EVENT_NO_LINE_ITEMS, event.getId()));
    }

    List<StockEventLineItem> selected = resolveSelectedLineItems(event, lineItemIdsToCancel);
    List<CancellationRule> rules = cancellationRules(event,
        findAlreadyCancelledIds(lineItemIdsToCancel), findCancelReasonIds(selected));

    List<StockEventCancellationLineErrorDto> errors = new ArrayList<>();
    for (StockEventLineItem lineItem : selected) {
      rules.stream()
          .map(rule -> rule.check(lineItem))
          .filter(Objects::nonNull)
          .findFirst()
          .ifPresent(errors::add);
    }

    if (!errors.isEmpty()) {
      throw new StockEventCancellationException(ERROR_EVENT_CANCELLATION_VALIDATION, errors);
    }
  }

  // The ordered checks a line item must pass to be cancellable; the first failing one is reported.
  // A new cancellation constraint is added here rather than by editing the validation loop.
  private List<CancellationRule> cancellationRules(StockEvent event,
      Set<UUID> alreadyCancelledIds, Set<UUID> cancelReasonIds) {
    return asList(
        lineItem -> isCancellationReason(lineItem, cancelReasonIds)
            ? lineError(lineItem, ERROR_EVENT_LINE_ITEM_IS_CANCELLATION, null) : null,
        lineItem -> !lineItem.isMovement()
            ? lineError(lineItem, ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, null) : null,
        lineItem -> alreadyCancelledIds.contains(lineItem.getId())
            ? lineError(lineItem, ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED, null) : null,
        lineItem -> blockedByPhysicalInventory(event, lineItem));
  }

  private StockEventCancellationLineErrorDto blockedByPhysicalInventory(StockEvent event,
      StockEventLineItem lineItem) {
    List<BlockingTransactionDto> blocking = findBlockingInventories(event, lineItem);
    return blocking.isEmpty()
        ? null
        : lineError(lineItem, ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, blocking);
  }

  private List<StockEventLineItem> resolveSelectedLineItems(StockEvent event,
      Collection<UUID> lineItemIdsToCancel) {
    Map<UUID, StockEventLineItem> byId = event.getLineItems().stream()
        .collect(toMap(StockEventLineItem::getId, lineItem -> lineItem));
    List<StockEventLineItem> selected = new ArrayList<>();
    for (UUID id : lineItemIdsToCancel) {
      StockEventLineItem lineItem = byId.get(id);
      if (lineItem == null) {
        throw new ValidationMessageException(
            new Message(ERROR_EVENT_LINE_ITEM_NOT_FOUND, id, event.getId()));
      }
      selected.add(lineItem);
    }
    return selected;
  }

  private Set<UUID> findAlreadyCancelledIds(Collection<UUID> lineItemIdsToCancel) {
    return stockEventLineItemRepository.findByReversesEventLineItemIdIn(lineItemIdsToCancel)
        .stream()
        .map(StockEventLineItem::getReversesEventLineItemId)
        .collect(toSet());
  }

  private Set<UUID> findCancelReasonIds(List<StockEventLineItem> lineItems) {
    Set<UUID> reasonIds = lineItems.stream()
        .map(StockEventLineItem::getReasonId)
        .filter(Objects::nonNull)
        .collect(toSet());
    if (reasonIds.isEmpty()) {
      return emptySet();
    }
    return reasonRepository.findByIdIn(reasonIds).stream()
        .filter(reason -> reason.getTags().contains(CANCEL_TAG))
        .map(StockCardLineItemReason::getId)
        .collect(toSet());
  }

  private boolean isCancellationReason(StockEventLineItem lineItem, Set<UUID> cancelReasonIds) {
    return lineItem.getReasonId() != null && cancelReasonIds.contains(lineItem.getReasonId());
  }

  private List<BlockingTransactionDto> findBlockingInventories(StockEvent event,
      StockEventLineItem lineItem) {
    // A physical inventory blocks cancellation when it is ordered after this movement on the stock
    // card, which is (occurredDate, then processedDate) - see StockCard.getLineItemsComparator().
    // Comparing occurredDate alone would miss a same-day inventory processed after the movement
    // (which already reflects it), letting the reversal double-apply the quantity.
    ZonedDateTime processedDate = event.getProcessedDate();
    List<PhysicalInventory> inventories = lineItem.getLotId() == null
        ? physicalInventoriesRepository.findSubmittedAfterForOrderableWithoutLot(
            event.getProgramId(), event.getFacilityId(), lineItem.getOrderableId(),
            lineItem.getOccurredDate(), processedDate)
        : physicalInventoriesRepository.findSubmittedAfterForOrderableAndLot(
            event.getProgramId(), event.getFacilityId(), lineItem.getOrderableId(),
            lineItem.getLotId(), lineItem.getOccurredDate(), processedDate);
    return inventories.stream()
        .map(inventory -> new BlockingTransactionDto(
            PHYSICAL_INVENTORY_TYPE, inventory.getOccurredDate(), inventory.getDocumentNumber()))
        .collect(toList());
  }

  private StockEventCancellationLineErrorDto lineError(StockEventLineItem lineItem,
      String messageKey, List<BlockingTransactionDto> blockingTransactions) {
    return new StockEventCancellationLineErrorDto(
        lineItem.getId(), messageKey, null, blockingTransactions);
  }

  // A single per-line cancellation check; returns an error, or null when the line passes.
  @FunctionalInterface
  private interface CancellationRule {
    StockEventCancellationLineErrorDto check(StockEventLineItem lineItem);
  }
}
