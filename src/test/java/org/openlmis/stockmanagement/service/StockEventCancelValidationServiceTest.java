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
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_IS_CANCELLATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE;
import static org.openlmis.stockmanagement.service.StockEventCancelValidationService.CANCEL_MOVEMENT_TAG;
import static org.openlmis.stockmanagement.service.StockEventCancelValidationService.PHYSICAL_INVENTORY_TYPE;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.BlockingTransactionDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockEventLineItemRepository;
import org.openlmis.stockmanagement.repository.custom.SubmittedPhysicalInventoryLine;

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

  private static final String PI_DOCUMENT_NUMBER = "PI-1";

  private final UUID programId = UUID.randomUUID();
  private final UUID facilityId = UUID.randomUUID();

  @Test
  public void shouldNotThrowWhenAllLineItemsCancellable() {
    StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    stubInventoryLines();

    service.validate(event, singletonList(lineItem.getId()));
  }

  @Test
  public void shouldReportNotCancellableWhenLineItemIsPhysicalInventory() {
    // a physical inventory line carries no source, destination or reason of its own
    StockEventLineItem lineItem = reasonLineItem(UUID.randomUUID(), null);
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, error.getMessageKey());
    assertNull(error.getBlockingTransactions());
  }

  @Test
  public void shouldNotThrowWhenLineItemIsManualAdjustment() {
    UUID reasonId = UUID.randomUUID();
    final StockEventLineItem lineItem = reasonLineItem(UUID.randomUUID(), reasonId);
    final StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(reasonRepository.findByIdIn(anyCollection()))
        .thenReturn(singletonList(reason(reasonId, ReasonCategory.ADJUSTMENT)));
    stubInventoryLines();

    service.validate(event, singletonList(lineItem.getId()));
  }

  @Test
  public void shouldReportNotCancellableWhenLineItemIsKitUnpack() {
    UUID reasonId = UUID.randomUUID();
    StockEventLineItem lineItem = reasonLineItem(UUID.randomUUID(), reasonId);
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(reasonRepository.findByIdIn(anyCollection()))
        .thenReturn(singletonList(reason(reasonId, ReasonCategory.AGGREGATION)));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, error.getMessageKey());
  }

  @Test
  public void shouldReportNotCancellableWhenEventHasNoOrigin() {
    // the requisition service pushes its adjustments on approval without an event origin
    UUID reasonId = UUID.randomUUID();
    StockEventLineItem lineItem = reasonLineItem(UUID.randomUUID(), reasonId);
    StockEvent event = eventWith(lineItem);
    event.setEventOrigin(null);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(reasonRepository.findByIdIn(anyCollection()))
        .thenReturn(singletonList(reason(reasonId, ReasonCategory.ADJUSTMENT)));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, error.getMessageKey());
  }

  @Test
  public void shouldReportNotCancellableWhenMovementEventHasNoOrigin() {
    StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    StockEvent event = eventWith(lineItem);
    event.setEventOrigin(null);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, error.getMessageKey());
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
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    stubInventoryLines(laterInventoryLineFor(lineItem, PI_DOCUMENT_NUMBER));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, error.getMessageKey());
    assertEquals(1, error.getBlockingTransactions().size());
    assertEquals(PHYSICAL_INVENTORY_TYPE, error.getBlockingTransactions().get(0).getType());
    assertEquals(PI_DOCUMENT_NUMBER, error.getBlockingTransactions().get(0).getDocumentNumber());
  }

  @Test
  public void shouldBlockLineItemWithoutLotOnlyByInventoryWithoutLot() {
    final StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .destinationId(UUID.randomUUID())
        .occurredDate(LocalDate.now())
        .build();
    lineItem.setId(UUID.randomUUID());
    final StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    stubInventoryLines(
        inventoryLine(lineItem.getOrderableId(), UUID.randomUUID(),
            lineItem.getOccurredDate().plusDays(1), null, "PI-LOT"),
        laterInventoryLineFor(lineItem, PI_DOCUMENT_NUMBER));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, error.getMessageKey());
    assertEquals(singletonList(PI_DOCUMENT_NUMBER), documentNumbersOf(error));
  }

  @Test
  public void shouldBlockOnlyByLaterInventoriesOfTheSameProductAndLot() {
    final StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    final StockEvent event = eventWith(lineItem);
    final LocalDate movementDate = lineItem.getOccurredDate();
    final SubmittedPhysicalInventoryLine later = laterInventoryLineFor(lineItem, "PI-LATER");
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    stubInventoryLines(
        inventoryLine(lineItem.getOrderableId(), lineItem.getLotId(), movementDate.minusDays(1),
            null, "PI-EARLIER"),
        later,
        // the same inventory counted the product and lot twice: reported once
        new SubmittedPhysicalInventoryLine(later.getPhysicalInventoryId(), later.getOccurredDate(),
            later.getDocumentNumber(), null, later.getOrderableId(), later.getLotId()),
        inventoryLine(lineItem.getOrderableId(), UUID.randomUUID(), movementDate.plusDays(1),
            null, "PI-OTHER-LOT"),
        inventoryLine(lineItem.getOrderableId(), null, movementDate.plusDays(1), null,
            "PI-NO-LOT"),
        inventoryLine(UUID.randomUUID(), lineItem.getLotId(), movementDate.plusDays(1), null,
            "PI-OTHER-PRODUCT"));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(singletonList("PI-LATER"), documentNumbersOf(error));
  }

  @Test
  public void shouldBlockSameDayInventoryOnlyWhenProcessedAfterTheMovement() {
    final StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    final StockEvent event = eventWith(lineItem);
    final ZonedDateTime processed = ZonedDateTime.parse("2026-01-01T10:00:00Z");
    event.setProcessedDate(processed);
    final LocalDate sameDay = lineItem.getOccurredDate();
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    stubInventoryLines(
        inventoryLine(lineItem.getOrderableId(), lineItem.getLotId(), sameDay,
            processed.plusHours(1), "PI-AFTER"),
        inventoryLine(lineItem.getOrderableId(), lineItem.getLotId(), sameDay,
            processed.minusHours(1), "PI-BEFORE"),
        inventoryLine(lineItem.getOrderableId(), lineItem.getLotId(), sameDay, processed,
            "PI-SAME-TIME"),
        inventoryLine(lineItem.getOrderableId(), lineItem.getLotId(), sameDay, null,
            "PI-WITHOUT-EVENT"));

    StockEventCancellationLineErrorDto error =
        assertSingleLineError(event, lineItem.getId());

    assertEquals(singletonList("PI-AFTER"), documentNumbersOf(error));
  }

  @Test
  public void shouldCompareEachLineItemWithItsOwnDateWhenLineItemsShareProductAndLot() {
    final StockEventLineItem earlier = issueLineItem(UUID.randomUUID());
    earlier.setOccurredDate(LocalDate.now().minusDays(5));
    final StockEventLineItem later = issueLineItem(UUID.randomUUID());
    later.setOrderableId(earlier.getOrderableId());
    later.setLotId(earlier.getLotId());
    final StockEvent event = eventWith(earlier, later);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    // recorded between the two movements: it reflects only the earlier one
    stubInventoryLines(inventoryLine(earlier.getOrderableId(), earlier.getLotId(),
        LocalDate.now().minusDays(2), null, "PI-BETWEEN"));

    Map<UUID, StockEventCancellationLineErrorDto> errors =
        service.evaluate(event, asList(earlier, later));

    assertEquals(singletonList(earlier.getId()), new ArrayList<>(errors.keySet()));
    assertEquals(singletonList("PI-BETWEEN"), documentNumbersOf(errors.get(earlier.getId())));
  }

  @Test
  public void shouldLookUpInventoriesOnceForAllLineItemsThatPassTheRules() {
    final StockEventLineItem first = issueLineItem(UUID.randomUUID());
    final StockEventLineItem earlier = issueLineItem(UUID.randomUUID());
    earlier.setOccurredDate(first.getOccurredDate().minusDays(3));
    final StockEventLineItem reversed = issueLineItem(UUID.randomUUID());
    reversed.setOccurredDate(first.getOccurredDate().minusDays(10));
    final StockEvent event = eventWith(first, earlier, reversed);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(singletonList(reversing(reversed)));

    service.evaluate(event, asList(first, earlier, reversed));

    verify(physicalInventoriesRepository).findSubmittedLinesOfOrderablesFrom(programId,
        facilityId, new HashSet<>(asList(first.getOrderableId(), earlier.getOrderableId())),
        earlier.getOccurredDate());
  }

  @Test
  public void shouldNotLookUpInventoriesWhenNoLineItemPassesTheRules() {
    UUID reasonId = UUID.randomUUID();
    StockEventLineItem kitUnpack = reasonLineItem(UUID.randomUUID(), reasonId);
    StockEvent event = eventWith(kitUnpack);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());
    when(reasonRepository.findByIdIn(anyCollection()))
        .thenReturn(singletonList(reason(reasonId, ReasonCategory.AGGREGATION)));

    service.evaluate(event, singletonList(kitUnpack));

    verifyNoInteractions(physicalInventoriesRepository);
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
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(singletonList(cancellation));
    stubInventoryLines(laterInventoryLineFor(blocked, PI_DOCUMENT_NUMBER));

    try {
      service.validate(event, asList(alreadyCancelled.getId(), blocked.getId()));
      fail("expected StockEventCancellationException");
    } catch (StockEventCancellationException ex) {
      assertEquals(2, ex.getLineErrors().size());
    }
  }

  @Test
  public void shouldReturnNoErrorWhenEvaluatingCancellableLineItem() {
    StockEventLineItem lineItem = issueLineItem(UUID.randomUUID());
    StockEvent event = eventWith(lineItem);
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(emptyList());

    Map<UUID, StockEventCancellationLineErrorDto> errors =
        service.evaluate(event, singletonList(lineItem));

    assertTrue(errors.isEmpty());
  }

  @Test
  public void shouldReturnFirstFailingRuleOfEachLineItemInInputOrderWhenEvaluating() {
    final UUID cancelReasonId = UUID.randomUUID();
    final UUID kitUnpackReasonId = UUID.randomUUID();
    final StockEventLineItem cancellable = issueLineItem(UUID.randomUUID());
    final StockEventLineItem cancellation = issueLineItem(UUID.randomUUID());
    cancellation.setReasonId(cancelReasonId);
    final StockEventLineItem kitUnpack = reasonLineItem(UUID.randomUUID(), kitUnpackReasonId);
    final StockEventLineItem reversed = issueLineItem(UUID.randomUUID());
    final StockEventLineItem blocked = issueLineItem(UUID.randomUUID());
    final StockEvent event = eventWith(cancellable, cancellation, kitUnpack, reversed, blocked);
    // the cancellation line is reversed too, which must not hide that it is a cancellation
    when(stockEventLineItemRepository.findByReversesEventLineItemIdIn(anyCollection()))
        .thenReturn(asList(reversing(cancellation), reversing(reversed)));
    when(reasonRepository.findByIdIn(anyCollection()))
        .thenReturn(asList(cancelReason(cancelReasonId),
            reason(kitUnpackReasonId, ReasonCategory.AGGREGATION)));
    stubInventoryLines(laterInventoryLineFor(blocked, PI_DOCUMENT_NUMBER));

    Map<UUID, StockEventCancellationLineErrorDto> errors = service.evaluate(event,
        asList(cancellable, cancellation, kitUnpack, reversed, blocked));

    assertEquals(asList(cancellation.getId(), kitUnpack.getId(), reversed.getId(),
        blocked.getId()), new ArrayList<>(errors.keySet()));
    assertEquals(ERROR_EVENT_LINE_ITEM_IS_CANCELLATION,
        errors.get(cancellation.getId()).getMessageKey());
    assertEquals(ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE,
        errors.get(kitUnpack.getId()).getMessageKey());
    assertEquals(ERROR_EVENT_LINE_ITEM_ALREADY_CANCELLED,
        errors.get(reversed.getId()).getMessageKey());
    StockEventCancellationLineErrorDto blockedError = errors.get(blocked.getId());
    assertEquals(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, blockedError.getMessageKey());
    assertEquals(blocked.getId(), blockedError.getStockEventLineItemId());
    assertEquals(PI_DOCUMENT_NUMBER,
        blockedError.getBlockingTransactions().get(0).getDocumentNumber());
  }

  @Test
  public void shouldNotQueryAnythingWhenEvaluatingNoLineItems() {
    StockEvent event = eventWith(issueLineItem(UUID.randomUUID()));

    Map<UUID, StockEventCancellationLineErrorDto> errors = service.evaluate(event, emptyList());

    assertTrue(errors.isEmpty());
    verifyNoInteractions(stockEventLineItemRepository, physicalInventoriesRepository,
        reasonRepository);
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
    event.setEventOrigin(EventOrigin.ADJUSTMENT);
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

  private void stubInventoryLines(SubmittedPhysicalInventoryLine... inventoryLines) {
    when(physicalInventoriesRepository.findSubmittedLinesOfOrderablesFrom(
        any(), any(), anyCollection(), any()))
        .thenReturn(asList(inventoryLines));
  }

  private SubmittedPhysicalInventoryLine laterInventoryLineFor(StockEventLineItem lineItem,
      String documentNumber) {
    return inventoryLine(lineItem.getOrderableId(), lineItem.getLotId(),
        lineItem.getOccurredDate().plusDays(1), null, documentNumber);
  }

  private SubmittedPhysicalInventoryLine inventoryLine(UUID orderableId, UUID lotId,
      LocalDate occurredDate, ZonedDateTime processedDate, String documentNumber) {
    return new SubmittedPhysicalInventoryLine(UUID.randomUUID(), occurredDate, documentNumber,
        processedDate, orderableId, lotId);
  }

  private List<String> documentNumbersOf(StockEventCancellationLineErrorDto error) {
    return error.getBlockingTransactions().stream()
        .map(BlockingTransactionDto::getDocumentNumber)
        .collect(toList());
  }

  private StockEventLineItem reversing(StockEventLineItem original) {
    return StockEventLineItem.builder()
        .reversesEventLineItemId(original.getId())
        .build();
  }

  private StockEventLineItem reasonLineItem(UUID id, UUID reasonId) {
    StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .reasonId(reasonId)
        .occurredDate(LocalDate.now())
        .build();
    lineItem.setId(id);
    return lineItem;
  }

  private StockCardLineItemReason reason(UUID id, ReasonCategory category) {
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name(category + " reason")
        .reasonType(ReasonType.DEBIT)
        .reasonCategory(category)
        .tags(emptySet())
        .build();
    reason.setId(id);
    return reason;
  }

  private StockCardLineItemReason cancelReason(UUID id) {
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name("Cancelled issue")
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .tags(singleton(CANCEL_MOVEMENT_TAG))
        .build();
    reason.setId(id);
    return reason;
  }
}
