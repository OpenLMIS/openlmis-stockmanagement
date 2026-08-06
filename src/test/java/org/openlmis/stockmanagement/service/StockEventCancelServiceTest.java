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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventCancelDto;
import org.openlmis.stockmanagement.dto.StockEventCancelLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.Message;

@RunWith(MockitoJUnitRunner.class)
public class StockEventCancelServiceTest {

  @Mock
  private StockEventsRepository stockEventsRepository;

  @Mock
  private StockEventCancelValidationService cancelValidationService;

  @Mock
  private CancellationReasonResolver reasonResolver;

  @Mock
  private StockEventProcessor stockEventProcessor;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private StockEventCancelService service;

  private final UUID eventId = UUID.randomUUID();
  private final UUID facilityId = UUID.randomUUID();
  private final UUID programId = UUID.randomUUID();

  @Test
  public void shouldBuildAndProcessCancellationEvent() {
    StockEvent event = eventWithIssueLine();
    StockEventLineItem original = event.getLineItems().get(0);
    StockCardLineItemReason reason = reason(ReasonType.CREDIT);
    StockEventCancelDto request = requestFor(original.getId(), reason.getId());
    UUID newEventId = UUID.randomUUID();
    ArgumentCaptor<StockEventDto> captor = ArgumentCaptor.forClass(StockEventDto.class);
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(reasonResolver.resolve(anyCollection(), anyMap()))
        .thenReturn(singletonMap(original.getId(), reason));
    when(stockEventProcessor.process(captor.capture())).thenReturn(newEventId);

    UUID result = service.cancel(eventId, request);

    assertEquals(newEventId, result);
    verify(cancelValidationService).validate(eq(event), any());
    StockEventDto cancellation = captor.getValue();
    assertEquals(facilityId, cancellation.getFacilityId());
    assertEquals(programId, cancellation.getProgramId());
    assertEquals("signature", cancellation.getSignature());
    assertTrue(cancellation.isActive());
    assertEquals(EventOrigin.ADJUSTMENT, cancellation.getEventOrigin());
    StockEventLineItemDto line = cancellation.getLineItems().get(0);
    assertEquals(original.getId(), line.getReversesEventLineItemId());
    assertEquals(reason.getId(), line.getReasonId());
    assertEquals(original.getOrderableId(), line.getOrderableId());
    assertEquals(original.getQuantity(), line.getQuantity());
    assertEquals(LocalDate.now(), line.getOccurredDate());
    assertNull(line.getSourceId());
    assertNull(line.getDestinationId());
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowWhenEventDoesNotExist() {
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.empty());

    service.cancel(eventId, requestFor(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenLineItemSelectedMoreThanOnce() {
    StockEvent event = eventWithIssueLine();
    UUID lineId = event.getLineItems().get(0).getId();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));

    StockEventCancelLineItemDto first =
        new StockEventCancelLineItemDto(lineId, UUID.randomUUID(), null);
    StockEventCancelLineItemDto duplicate =
        new StockEventCancelLineItemDto(lineId, UUID.randomUUID(), null);

    service.cancel(eventId, new StockEventCancelDto("signature", asList(first, duplicate)));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenLineItemHasNoStableId() {
    StockEvent event = eventWithIssueLine();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));

    // A movement recorded before the reversal feature exposes a null stockEventLineItemId.
    service.cancel(eventId, requestFor(null, UUID.randomUUID()));
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldThrowWhenUserHasNoCancelPermission() {
    StockEvent event = eventWithIssueLine();
    StockEventLineItem original = event.getLineItems().get(0);
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    doThrow(new PermissionMessageException(new Message(ERROR_NO_FOLLOWING_PERMISSION)))
        .when(permissionService).canCancelStockEvent(any(), any());

    service.cancel(eventId, requestFor(original.getId(), UUID.randomUUID()));
  }

  private StockEvent eventWithIssueLine() {
    StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .lotId(UUID.randomUUID())
        .quantity(10)
        .destinationId(UUID.randomUUID())
        .occurredDate(LocalDate.now())
        .build();
    lineItem.setId(UUID.randomUUID());
    StockEvent event = new StockEvent();
    event.setId(eventId);
    event.setFacilityId(facilityId);
    event.setProgramId(programId);
    event.setLineItems(singletonList(lineItem));
    return event;
  }

  private StockCardLineItemReason reason(ReasonType type) {
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name("Cancellation " + type)
        .reasonType(type)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .build();
    reason.setId(UUID.randomUUID());
    return reason;
  }

  private StockEventCancelDto requestFor(UUID lineItemId, UUID reasonId) {
    StockEventCancelLineItemDto lineItem =
        new StockEventCancelLineItemDto(lineItemId, reasonId, null);
    return new StockEventCancelDto("signature", singletonList(lineItem));
  }
}
