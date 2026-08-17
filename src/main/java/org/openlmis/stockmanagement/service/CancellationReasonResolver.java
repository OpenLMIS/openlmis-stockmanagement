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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_VALIDATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventCancelLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.springframework.stereotype.Service;

/**
 * Loads and validates the cancellation reason chosen for each line item selected for cancellation.
 * A valid reason is a cancel-tagged ADJUSTMENT reason whose type counters the original movement: an
 * issue is reversed with a credit, a receive with a debit.
 */
@Service
@RequiredArgsConstructor
public class CancellationReasonResolver {

  private final StockCardLineItemReasonRepository reasonRepository;

  /**
   * Resolves the validated cancellation reason for each requested line item.
   *
   * @param requested    the line items selected for cancellation with their chosen reason.
   * @param originalById the event's original line items keyed by id.
   * @return the validated cancellation reason keyed by original line item id.
   */
  public Map<UUID, StockCardLineItemReason> resolve(
      Collection<StockEventCancelLineItemDto> requested,
      Map<UUID, StockEventLineItem> originalById) {
    Map<UUID, StockCardLineItemReason> loaded = loadReasons(requested);
    Map<UUID, StockCardLineItemReason> resolved = new HashMap<>();
    List<StockEventCancellationLineErrorDto> errors = new ArrayList<>();
    for (StockEventCancelLineItemDto line : requested) {
      StockEventLineItem original = originalById.get(line.getStockEventLineItemId());
      StockCardLineItemReason reason = reasonOrError(line, original, loaded, errors);
      if (reason != null) {
        resolved.put(line.getStockEventLineItemId(), reason);
      }
    }
    // Collect every bad reason and report them together, per line, in the same shape as the other
    // cancellation checks (AC#9) - the client marks each blocking line rather than only the first.
    if (!errors.isEmpty()) {
      throw new StockEventCancellationException(ERROR_EVENT_CANCELLATION_VALIDATION, errors);
    }
    return resolved;
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

  // Returns the validated cancellation reason for the line, or null - recording a per-line error -
  // when the reason is missing or is not a cancel-tagged ADJUSTMENT that counters the movement.
  private StockCardLineItemReason reasonOrError(StockEventCancelLineItemDto requested,
      StockEventLineItem original, Map<UUID, StockCardLineItemReason> reasonsById,
      List<StockEventCancellationLineErrorDto> errors) {
    UUID reasonId = requested.getReasonId();
    if (reasonId == null) {
      errors.add(lineError(requested, ERROR_EVENT_CANCELLATION_REASON_REQUIRED));
      return null;
    }
    StockCardLineItemReason reason = reasonsById.get(reasonId);
    if (reason == null
        || !reason.isAdjustmentReasonCategory()
        || !reason.getTags().contains(StockEventCancelValidationService.CANCEL_TAG)
        || !countersMovement(original, reason)) {
      errors.add(lineError(requested, ERROR_EVENT_CANCELLATION_REASON_INVALID));
      return null;
    }
    return reason;
  }

  private StockEventCancellationLineErrorDto lineError(StockEventCancelLineItemDto requested,
      String messageKey) {
    return new StockEventCancellationLineErrorDto(
        requested.getStockEventLineItemId(), messageKey, null, null);
  }

  private boolean countersMovement(StockEventLineItem original, StockCardLineItemReason reason) {
    // an issue (has a destination) is reversed with a credit; a receive (has a source) with a debit
    return original.isIssue()
        ? reason.isCreditReasonType()
        : reason.isDebitReasonType();
  }
}
