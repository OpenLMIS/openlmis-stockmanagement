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
import lombok.Value;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;
import org.springframework.stereotype.Service;

/**
 * Resolves, per page and read-side only, the cross-links between an issue/receive event and the
 * event that cancelled it (SELV3-861, Scenario 2 - the original event is left untouched):
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

  // "Reversed by": for an original line, the cancellation event that reversed it. Matched per line
  // (event + orderable + lot) so a partial cancellation marks only the line that was cancelled.
  private void attachReversedBy(List<StockCardLineItemDto> lineItems) {
    Set<UUID> eventIds = new HashSet<>();
    Set<UUID> orderableIds = new HashSet<>();
    collectEventAndOrderableIds(lineItems, eventIds, orderableIds);
    if (eventIds.isEmpty()) {
      return;
    }
    Map<LineKey, UUID> originLineIdByKey = originLineIdsByKey(eventIds, orderableIds);
    Map<UUID, StockEvent> cancellationByOriginLine =
        cancellationEventsByOriginLine(originLineIdByKey.values());
    for (StockCardLineItemDto dto : lineItems) {
      setReversedBy(dto, originLineIdByKey, cancellationByOriginLine);
    }
  }

  private void collectEventAndOrderableIds(List<StockCardLineItemDto> lineItems,
      Set<UUID> eventIds, Set<UUID> orderableIds) {
    for (StockCardLineItemDto dto : lineItems) {
      LineKey key = LineKey.of(dto);
      if (key != null) {
        eventIds.add(key.getEventId());
        orderableIds.add(key.getOrderableId());
      }
    }
  }

  // (event, orderable, lot) -> original line id
  private Map<LineKey, UUID> originLineIdsByKey(Set<UUID> eventIds, Set<UUID> orderableIds) {
    Map<LineKey, UUID> map = new HashMap<>();
    for (StockEventLineItem line : stockEventLineItemRepository
        .findByStockEventIdInAndOrderableIdIn(eventIds, orderableIds)) {
      map.putIfAbsent(
          new LineKey(line.getStockEvent().getId(), line.getOrderableId(), line.getLotId()),
          line.getId());
    }
    return map;
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

  private void setReversedBy(StockCardLineItemDto dto, Map<LineKey, UUID> originLineIdByKey,
      Map<UUID, StockEvent> cancellationByOriginLine) {
    LineKey key = LineKey.of(dto);
    if (key == null) {
      return;
    }
    StockEvent cancellation = cancellationByOriginLine.get(originLineIdByKey.get(key));
    if (cancellation != null) {
      dto.setCancellationEventId(cancellation.getId());
      dto.setCancellationEventDocumentNumber(cancellation.getDocumentNumber());
    }
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

  // Identifies a shown line by (origin event, orderable, lot): the tuple that matches a stock
  // card line item to its stock event line item. A value object (not a stringly-typed key) so a
  // null lot compares as an absent value rather than the literal text "null".
  @Value
  private static class LineKey {
    UUID eventId;
    UUID orderableId;
    UUID lotId;

    // The key of a shown line, or null when the origin event / owning card is unavailable.
    static LineKey of(StockCardLineItemDto dto) {
      StockEvent originEvent = originEventOf(dto);
      if (originEvent == null || dto.getLineItem().getStockCard() == null) {
        return null;
      }
      return new LineKey(originEvent.getId(),
          dto.getLineItem().getStockCard().getOrderableId(),
          dto.getLineItem().getStockCard().getLotId());
    }
  }
}
