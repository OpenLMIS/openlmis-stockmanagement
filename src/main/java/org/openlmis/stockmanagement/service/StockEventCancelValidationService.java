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
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_VALIDATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_IS_CANCELLATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_NO_LINE_ITEMS;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.BlockingTransactionDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;
import org.openlmis.stockmanagement.repository.custom.SubmittedPhysicalInventoryLine;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Service;

/**
 * Validates that selected line items of a stock event may be cancelled - a movement recorded as an
 * issue, a receive or an adjustment. Every per-line failure is collected so the caller learns about
 * all blocking line items at once. The same rules serve the submit path ({@link #validate}) and
 * callers that only need to know which lines can be cancelled ({@link #evaluate}).
 */
@Service
@RequiredArgsConstructor
public class StockEventCancelValidationService {

  static final String CANCEL_MOVEMENT_TAG = "cancelMovement";
  static final String CANCEL_ADJUSTMENT_TAG = "cancelAdjustment";
  // Add a new cancel scope here and every "is this a cancel reason" check picks it up.
  private static final List<String> CANCEL_TAGS =
      asList(CANCEL_MOVEMENT_TAG, CANCEL_ADJUSTMENT_TAG);
  static final String PHYSICAL_INVENTORY_TYPE = "PHYSICAL_INVENTORY";

  private final StockEventLineItemRepository stockEventLineItemRepository;
  private final PhysicalInventoriesRepository physicalInventoriesRepository;
  private final StockCardLineItemReasonRepository reasonRepository;

  /**
   * Validates that the selected line items of the given event can be cancelled. Collects every
   * per-line failure and, if any, throws a single {@link StockEventCancellationException}.
   *
   * @param event               the original stock event being cancelled.
   * @param lineItemIdsToCancel distinct ids of the event's line items selected for cancellation.
   */
  public void validate(StockEvent event, Collection<UUID> lineItemIdsToCancel) {
    if (lineItemIdsToCancel == null || lineItemIdsToCancel.isEmpty()) {
      throw new ValidationMessageException(new Message(ERROR_EVENT_NO_LINE_ITEMS, event.getId()));
    }

    List<StockEventLineItem> selected = resolveSelectedLineItems(event, lineItemIdsToCancel);
    Map<UUID, StockEventCancellationLineErrorDto> errors = evaluate(event, selected);

    if (!errors.isEmpty()) {
      throw new StockEventCancellationException(ERROR_EVENT_CANCELLATION_VALIDATION,
          new ArrayList<>(errors.values()));
    }
  }

  /**
   * Checks the given line items of an event against the cancellation rules without throwing, so a
   * caller can learn which lines cannot be cancelled before anything is submitted. Applies the
   * same rules as {@link #validate}.
   *
   * @param event     the stock event the line items belong to.
   * @param lineItems distinct line items of that event.
   * @return for each failing line item, the error of the first rule it fails, keyed by line item
   *         id in the order of {@code lineItems}. Line items that can be cancelled are absent.
   */
  public Map<UUID, StockEventCancellationLineErrorDto> evaluate(StockEvent event,
      Collection<StockEventLineItem> lineItems) {
    if (lineItems.isEmpty()) {
      return emptyMap();
    }

    Set<UUID> lineItemIds = lineItems.stream()
        .map(StockEventLineItem::getId)
        .collect(toSet());
    List<CancellationRule> rules = cancellationRules(event,
        findAlreadyCancelledIds(lineItemIds), findReasons(lineItems));

    Map<UUID, StockEventCancellationLineErrorDto> ruleErrors = new HashMap<>();
    List<StockEventLineItem> passingRules = new ArrayList<>();
    for (StockEventLineItem lineItem : lineItems) {
      Optional<StockEventCancellationLineErrorDto> ruleError = rules.stream()
          .map(rule -> rule.check(lineItem))
          .filter(Objects::nonNull)
          .findFirst();
      if (ruleError.isPresent()) {
        ruleErrors.put(lineItem.getId(), ruleError.get());
      } else {
        passingRules.add(lineItem);
      }
    }

    // Checked last, and for all line items that passed the rules at once (one query per event).
    Map<UUID, List<BlockingTransactionDto>> blockingInventories =
        findBlockingInventories(event, passingRules);

    Map<UUID, StockEventCancellationLineErrorDto> errors = new LinkedHashMap<>();
    for (StockEventLineItem lineItem : lineItems) {
      StockEventCancellationLineErrorDto error = ruleErrors.get(lineItem.getId());
      if (error == null && blockingInventories.containsKey(lineItem.getId())) {
        error = lineError(lineItem, ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY,
            blockingInventories.get(lineItem.getId()));
      }
      if (error != null) {
        errors.put(lineItem.getId(), error);
      }
    }
    return errors;
  }

  // The ordered checks a line item must pass to be cancellable; the first failing one is reported.
  // A new cancellation constraint is added here rather than by editing the validation loop. The
  // physical inventory check runs after these, for all remaining line items in one query.
  private List<CancellationRule> cancellationRules(StockEvent event,
      Set<UUID> alreadyCancelledIds, Map<UUID, StockCardLineItemReason> reasonsById) {
    return asList(
        // Must stay first: a cancellation is an adjustment too, so this is the only rule keeping a
        // reversal from being reversed.
        lineItem -> isCancellationReason(lineItem, reasonsById)
            ? lineError(lineItem, ERROR_EVENT_LINE_ITEM_IS_CANCELLATION, null) : null,
        lineItem -> !isCancellable(event, lineItem, reasonsById)
            ? lineError(lineItem, ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, null) : null,
        lineItem -> alreadyCancelledIds.contains(lineItem.getId())
            ? lineError(lineItem, ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED, null) : null);
  }

  private boolean isCancellable(StockEvent event, StockEventLineItem lineItem,
      Map<UUID, StockCardLineItemReason> reasonsById) {
    if (event.getEventOrigin() == null) {
      return false;
    }
    if (lineItem.isMovement()) {
      return true;
    }
    StockCardLineItemReason reason = reasonsById.get(lineItem.getReasonId());
    return reason != null && reason.isAdjustmentReasonCategory();
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

  private Map<UUID, StockCardLineItemReason> findReasons(
      Collection<StockEventLineItem> lineItems) {
    Set<UUID> reasonIds = lineItems.stream()
        .map(StockEventLineItem::getReasonId)
        .filter(Objects::nonNull)
        .collect(toSet());
    if (reasonIds.isEmpty()) {
      return emptyMap();
    }
    return reasonRepository.findByIdIn(reasonIds).stream()
        .collect(toMap(StockCardLineItemReason::getId, identity()));
  }

  private boolean isCancellationReason(StockEventLineItem lineItem,
      Map<UUID, StockCardLineItemReason> reasonsById) {
    return isCancelReason(reasonsById.get(lineItem.getReasonId()));
  }

  static boolean isCancelReason(StockCardLineItemReason reason) {
    return reason != null && reason.getTags().stream().anyMatch(CANCEL_TAGS::contains);
  }

  // Returns, for each given line item that has any, the submitted physical inventories of the same
  // product and lot recorded after it, keyed by line item id.
  private Map<UUID, List<BlockingTransactionDto>> findBlockingInventories(StockEvent event,
      List<StockEventLineItem> lineItems) {
    if (lineItems.isEmpty()) {
      return emptyMap();
    }

    Set<UUID> orderableIds = lineItems.stream()
        .map(StockEventLineItem::getOrderableId)
        .collect(toSet());
    LocalDate earliestOccurredDate = lineItems.stream()
        .map(StockEventLineItem::getOccurredDate)
        .min(naturalOrder())
        .get();
    Map<UUID, List<SubmittedPhysicalInventoryLine>> inventoryLinesByOrderable =
        physicalInventoriesRepository.findSubmittedLinesOfOrderablesFrom(event.getProgramId(),
            event.getFacilityId(), orderableIds, earliestOccurredDate)
            .stream()
            .collect(groupingBy(SubmittedPhysicalInventoryLine::getOrderableId));

    Map<UUID, List<BlockingTransactionDto>> blocking = new HashMap<>();
    for (StockEventLineItem lineItem : lineItems) {
      Map<UUID, BlockingTransactionDto> byInventory = new LinkedHashMap<>();
      inventoryLinesByOrderable.getOrDefault(lineItem.getOrderableId(), emptyList()).stream()
          .filter(inventoryLine -> Objects.equals(inventoryLine.getLotId(), lineItem.getLotId()))
          .filter(inventoryLine -> isRecordedAfter(inventoryLine, lineItem, event))
          .forEach(inventoryLine -> byInventory.putIfAbsent(
              inventoryLine.getPhysicalInventoryId(),
              new BlockingTransactionDto(PHYSICAL_INVENTORY_TYPE,
                  inventoryLine.getOccurredDate(), inventoryLine.getDocumentNumber())));
      if (!byInventory.isEmpty()) {
        blocking.put(lineItem.getId(), new ArrayList<>(byInventory.values()));
      }
    }
    return blocking;
  }

  // A physical inventory blocks cancellation when it is ordered after the movement on the stock
  // card, which is (occurredDate, then processedDate) - see StockCard.getLineItemsComparator().
  // Comparing occurredDate alone would miss a same-day inventory processed after the movement
  // (which already reflects it), letting the reversal double-apply the quantity. An inventory
  // without a stock event has no processed date, so on the same day it does not block.
  private boolean isRecordedAfter(SubmittedPhysicalInventoryLine inventoryLine,
      StockEventLineItem lineItem, StockEvent event) {
    if (inventoryLine.getOccurredDate().isAfter(lineItem.getOccurredDate())) {
      return true;
    }
    ZonedDateTime inventoryProcessedDate = inventoryLine.getProcessedDate();
    return inventoryLine.getOccurredDate().isEqual(lineItem.getOccurredDate())
        && inventoryProcessedDate != null && event.getProcessedDate() != null
        && inventoryProcessedDate.isAfter(event.getProcessedDate());
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
