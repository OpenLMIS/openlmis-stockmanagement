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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;

@RunWith(MockitoJUnitRunner.class)
public class CancellationLinkResolverTest {

  @Mock
  private StockEventLineItemRepository stockEventLineItemRepository;

  @InjectMocks
  private CancellationLinkResolver resolver;

  @Test
  public void shouldAttachReversingLinkForCancellationLine() {
    StockEvent originEvent = event("DOC-ORIGIN");
    StockEventLineItem originLine = line(originEvent, null);
    StockEvent cancellationEvent = event("DOC-CANCEL");
    StockEventLineItem cancellationLine = line(cancellationEvent, originLine.getId());
    StockCardLineItemDto dto = cancellationDto(cancellationEvent);

    when(stockEventLineItemRepository
        .findByStockEventIdInAndReversesEventLineItemIdIsNotNull(any()))
        .thenReturn(singletonList(cancellationLine));
    when(stockEventLineItemRepository.findAllById(any()))
        .thenReturn(singletonList(originLine));

    resolver.attachCancellationLinks(singletonList(dto));

    assertEquals(originEvent.getId(), dto.getReversedEventId());
    assertEquals("DOC-ORIGIN", dto.getReversedEventDocumentNumber());
  }

  @Test
  public void shouldAttachReversedByLinkForCancelledOriginLine() {
    UUID orderableId = UUID.randomUUID();
    StockEvent originEvent = event("DOC-ORIGIN");
    StockEventLineItem originLine = line(originEvent, null);
    originLine.setOrderableId(orderableId);
    StockEvent cancellationEvent = event("DOC-CANCEL");
    StockEventLineItem cancellationLine = line(cancellationEvent, originLine.getId());
    StockCardLineItemDto dto = originDto(originEvent, orderableId);

    when(stockEventLineItemRepository.findByStockEventIdInAndOrderableIdIn(any(), any()))
        .thenReturn(singletonList(originLine));
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(any()))
        .thenReturn(singletonList(cancellationLine));

    resolver.attachCancellationLinks(singletonList(dto));

    assertEquals(cancellationEvent.getId(), dto.getCancellationEventId());
    assertEquals("DOC-CANCEL", dto.getCancellationEventDocumentNumber());
  }

  @Test
  public void shouldNotAttachWhenEventIsNotCancellation() {
    StockCardLineItemDto dto = cancellationDto(event("DOC-1"));
    when(stockEventLineItemRepository
        .findByStockEventIdInAndReversesEventLineItemIdIsNotNull(any()))
        .thenReturn(emptyList());

    resolver.attachCancellationLinks(singletonList(dto));

    assertNull(dto.getReversedEventId());
    assertNull(dto.getCancellationEventId());
  }

  private StockEvent event(String documentNumber) {
    StockEvent event = new StockEvent();
    event.setId(UUID.randomUUID());
    event.setDocumentNumber(documentNumber);
    return event;
  }

  private StockEventLineItem line(StockEvent stockEvent, UUID reversesEventLineItemId) {
    StockEventLineItem line = new StockEventLineItem();
    line.setId(UUID.randomUUID());
    line.setStockEvent(stockEvent);
    line.setReversesEventLineItemId(reversesEventLineItemId);
    return line;
  }

  private StockCardLineItemDto cancellationDto(StockEvent originEvent) {
    StockCardLineItem cardLine = StockCardLineItem.builder().originEvent(originEvent).build();
    return StockCardLineItemDto.builder().lineItem(cardLine).build();
  }

  private StockCardLineItemDto originDto(StockEvent originEvent, UUID orderableId) {
    StockCard card = StockCard.builder().orderableId(orderableId).build();
    StockCardLineItem cardLine = StockCardLineItem.builder()
        .originEvent(originEvent).stockCard(card).build();
    return StockCardLineItemDto.builder().lineItem(cardLine).build();
  }
}
