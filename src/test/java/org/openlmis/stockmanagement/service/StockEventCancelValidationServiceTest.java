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
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_IS_CANCELLATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE;
import static org.openlmis.stockmanagement.service.StockEventCancelValidationService.CANCEL_TAG;
import static org.openlmis.stockmanagement.service.StockEventCancelValidationService.PHYSICAL_INVENTORY_TYPE;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class StockEventCancelValidationServiceTest {

  @Mock
  private StockEventLineItemRepository stockEventLineItemRepository;

  @Mock
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @InjectMocks
  private StockEventCancelValidationService service;

  private final UUID programId = UUID.randomUUID();
  private final UUID facilityId = UUID.randomUUID();

  @Test
  public void shouldNotThrowWhenAllLineItemsCancellable() {
    StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(physicalInventoriesRepository.findSubmittedAfterForOrderableAndLot(
        any(), any(), any(), any(), any(), any()))
        .thenReturn(emptyList());

    service.validate(event, singletonList(lineItem.getId()));
  }

  @Test
  public void shouldReportNotCancellableWhenLineItemIsNotIssueOrReceive() {
    StockEventLineItem lineItem = adjustmentLineItem(UUID.randomUUID());
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, error.getMessageKey());
    assertNull(error.getBlockingTransactions());
  }

  @Test
  public void shouldReportAlreadyCancelledWhenLineItemWasReversed() {
    StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    StockEvent event = eventWith(lineItem);
    StockEventLineItem cancellation = StockEventLineItem.builder()
        .reversesEventLineItemId(lineItem.getId())
        .build();
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(singletonList(cancellation));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED, error.getMessageKey());
  }

  @Test
  public void shouldReportBlockedWhenNewerPhysicalInventoryExists() {
    final StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    final StockEvent event = eventWith(lineItem);
    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setOccurredDate(LocalDate.now());
    inventory.setDocumentNumber("PI-1");
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(physicalInventoriesRepository.findSubmittedAfterForOrderableAndLot(
        any(), any(), any(), any(), any(), any()))
        .thenReturn(singletonList(inventory));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, error.getMessageKey());
    assertEquals(1, error.getBlockingTransactions().size());
    assertEquals(PHYSICAL_INVENTORY_TYPE, error.getBlockingTransactions().get(0).getType());
    assertEquals("PI-1", error.getBlockingTransactions().get(0).getDocumentNumber());
  }

  @Test
  public void shouldUseNoLotQueryWhenLineItemHasNoLot() {
    final StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .destinationId(UUID.randomUUID())
        .occurredDate(LocalDate.now())
        .build();
    lineItem.setId(UUID.randomUUID());
    final StockEvent event = eventWith(lineItem);
    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setOccurredDate(LocalDate.now());
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(physicalInventoriesRepository.findSubmittedAfterForOrderableWithoutLot(
        any(), any(), any(), any(), any()))
        .thenReturn(singletonList(inventory));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, error.getMessageKey());
  }

  @Test
  public void shouldReportIsCancellationWhenOriginalReasonIsCancelTagged() {
    UUID reasonId = UUID.randomUUID();
    StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    lineItem.setReasonId(reasonId);
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(reasonRepository.findByIdIn(anyCollection()))
        .thenReturn(singletonList(cancelReason(reasonId)));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_IS_CANCELLATION, error.getMessageKey());
  }

  @Test
  public void shouldCollectErrorsForEveryFailingLineItem() {
    final StockEventLineItem alreadyCancelled = issueLineItem(UUID.randomUUID());
    final StockEventLineItem blocked = issueLineItem(UUID.randomUUID());
    final StockEvent event = eventWith(alreadyCancelled, blocked);
    StockEventLineItem cancellation = StockEventLineItem.builder()
        .reversesEventLineItemId(alreadyCancelled.getId())
        .build();
    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setOccurredDate(LocalDate.now());
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(singletonList(cancellation));
    when(physicalInventoriesRepository.findSubmittedAfterForOrderableAndLot(
        any(), any(), any(), any(), any(), any()))
        .thenReturn(singletonList(inventory));

    try {
      service.validate(event, asList(alreadyCancelled.getId(), blocked.getId()));
      fail("expected StockEventCancellationException");
    } catch (StockEventCancellationException ex) {
      assertEquals(2, ex.getLineErrors().size());
    }
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenLineItemDoesNotBelongToEvent() {
    StockEvent event = eventWith(issueLineItem(UUID.randomUUID()));

    service.validate(event, singletonList(UUID.randomUUID()));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenNoLineItemsSelected() {
    StockEvent event = eventWith(issueLineItem(UUID.randomUUID()));

    service.validate(event, emptyList());
  }

  private StockEventCancellationLineErrorDto assertSingleLineError(StockEvent event, UUID lineId) {
    try {
      service.validate(event, singletonList(lineId));
      throw new AssertionError("expected StockEventCancellationException");
    } catch (StockEventCancellationException ex) {
      List<StockEventCancellationLineErrorDto> errors = ex.getLineErrors();
      assertEquals(1, errors.size());
      assertEquals(lineId, errors.get(0).getStockEventLineItemId());
      return errors.get(0);
    }
  }

  private StockEvent eventWith(StockEventLineItem... lineItems) {
    StockEvent event = new StockEvent();
    event.setId(UUID.randomUUID());
    event.setProgramId(programId);
    event.setFacilityId(facilityId);
    event.setLineItems(asList(lineItems));
    return event;
  }

  private StockEventLineItem issueLineItem(UUID id) {
    StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .lotId(UUID.randomUUID())
        .destinationId(UUID.randomUUID())
        .occurredDate(LocalDate.now())
        .build();
    lineItem.setId(id);
    return lineItem;
  }

  private StockEventLineItem adjustmentLineItem(UUID id) {
    StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .occurredDate(LocalDate.now())
        .build();
    lineItem.setId(id);
    return lineItem;
  }

  private StockCardLineItemReason cancelReason(UUID id) {
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name("Cancelled issue")
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .tags(singletonList(CANCEL_TAG))
        .build();
    reason.setId(id);
    return reason;
  }
}
