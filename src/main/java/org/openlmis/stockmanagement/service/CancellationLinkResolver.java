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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;
import org.springframework.stereotype.Service;

/**
 * Resolves, per page and read-side only, the cross-links between an issue/receive event and the
 * event that cancelled it (resolved on read; the original event is left untouched):
 * "Reversing" (a cancellation line links to the original event it reverses) and "Reversed by"
 * (an original line links to the cancellation event that reversed it).
 */
@Service
@RequiredArgsConstructor
public class CancellationLinkResolver {

  private final StockEventLineItemRepository stockEventLineItemRepository;

  /**
   * Sets both cancellation cross-links on the given stock card line item DTOs, in batch.
   *
   * @param lineItems the stock card line item DTOs shown on the current page.
   */
  public void attachCancellationLinks(List<StockCardLineItemDto> lineItems) {
    if (lineItems == null || lineItems.isEmpty()) {
      return;
    }
    attachReversing(lineItems);
    attachReversedBy(lineItems);
  }

  // "Reversing": for a cancellation line, the original event it reverses (once per cancellation
  // event, since a cancellation reverses exactly one original event).
  private void attachReversing(List<StockCardLineItemDto> lineItems) {
    Map<UUID, StockEvent> reversedByCancellationEvent = reversedEvents(lineItems);
    for (StockCardLineItemDto lineItem : lineItems) {
      StockEvent originEvent = originEventOf(lineItem);
      StockEvent reversed = originEvent == null
          ? null : reversedByCancellationEvent.get(originEvent.getId());
      if (reversed != null) {
        lineItem.setReversedEventId(reversed.getId());
        lineItem.setReversedEventDocumentNumber(reversed.getDocumentNumber());
      }
    }
  }

  // "Reversed by": for an original line, the cancellation event that reversed it, keyed by the
  // line's own stock event line item id - stable and unique, unlike an orderable+lot match - so a
  // partial cancellation marks only the line that was actually cancelled.
  private void attachReversedBy(List<StockCardLineItemDto> lineItems) {
    Set<UUID> lineItemIds = new HashSet<>();
    for (StockCardLineItemDto dto : lineItems) {
      if (dto.getStockEventLineItemId() != null) {
        lineItemIds.add(dto.getStockEventLineItemId());
      }
    }
    if (lineItemIds.isEmpty()) {
      return;
    }
    Map<UUID, StockEvent> cancellationByOriginLine = cancellationEventsByOriginLine(lineItemIds);
    for (StockCardLineItemDto dto : lineItems) {
      StockEvent cancellation = cancellationByOriginLine.get(dto.getStockEventLineItemId());
      if (cancellation != null) {
        dto.setCancellationEventId(cancellation.getId());
        dto.setCancellationEventDocumentNumber(cancellation.getDocumentNumber());
      }
    }
  }

  // original line id -> the cancellation event that reverses it
  private Map<UUID, StockEvent> cancellationEventsByOriginLine(Collection<UUID> originLineIds) {
    Map<UUID, StockEvent> map = new HashMap<>();
    for (StockEventLineItem cancellationLine :
        stockEventLineItemRepository.findByReversesEventLineItemIdIn(originLineIds)) {
      map.putIfAbsent(cancellationLine.getReversesEventLineItemId(),
          cancellationLine.getStockEvent());
    }
    return map;
  }

  private Map<UUID, StockEvent> reversedEvents(List<StockCardLineItemDto> lineItems) {
    Set<UUID> eventIds = new HashSet<>();
    for (StockCardLineItemDto dto : lineItems) {
      StockEvent originEvent = originEventOf(dto);
      if (originEvent != null) {
        eventIds.add(originEvent.getId());
      }
    }
    if (eventIds.isEmpty()) {
      return emptyMap();
    }

    Map<UUID, UUID> reversedLineByEvent = new HashMap<>();
    for (StockEventLineItem cancellationLine : stockEventLineItemRepository
        .findByStockEventIdInAndReversesEventLineItemIdIsNotNull(eventIds)) {
      reversedLineByEvent.putIfAbsent(cancellationLine.getStockEvent().getId(),
          cancellationLine.getReversesEventLineItemId());
    }

    Map<UUID, StockEvent> eventByOriginLine = new HashMap<>();
    stockEventLineItemRepository.findAllById(reversedLineByEvent.values())
        .forEach(line -> eventByOriginLine.put(line.getId(), line.getStockEvent()));

    Map<UUID, StockEvent> result = new HashMap<>();
    reversedLineByEvent.forEach((cancellationEventId, originLineId) -> {
      StockEvent originEvent = eventByOriginLine.get(originLineId);
      if (originEvent != null) {
        result.put(cancellationEventId, originEvent);
      }
    });
    return result;
  }

  private static StockEvent originEventOf(StockCardLineItemDto dto) {
    return dto.getLineItem() == null ? null : dto.getLineItem().getOriginEvent();
  }
}
