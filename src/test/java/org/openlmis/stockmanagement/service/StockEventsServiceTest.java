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
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_RECORDED_BEFORE_REVERSAL;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.BlockingTransactionDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.dto.StockEventHistoryDto;
import org.openlmis.stockmanagement.dto.StockEventLineDetailDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.repository.custom.StockEventLineItemAggregate;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class StockEventsServiceTest {

  private static final String DOC_1 = "DOC-1";
  private static final String DOC_2 = "DOC-2";
  private static final String DOC_3 = "DOC-3";
  private static final String ALICE = "alice";
  private static final String LOCALIZED = "localized";
  private static final String PRODUCT_CODE = "ABC";

  @Mock
  private StockEventsRepository stockEventsRepository;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private StockCardLineItemRepository stockCardLineItemRepository;

  @Mock
  private StockCardService stockCardService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private StockEventCancelValidationService cancelValidationService;

  @Mock
  private MessageService messageService;

  @Captor
  private ArgumentCaptor<Collection<StockEventLineItem>> evaluatedCaptor;

  @InjectMocks
  private StockEventsService stockEventsService;

  private Pageable pageable = PageRequest.of(0, 10);

  @Before
  public void setUp() {
    lenient().when(messageService.localize(any(Message.class)))
        .thenAnswer(invocation -> invocation.<Message>getArgument(0)
            .new LocalizedMessage(LOCALIZED));
  }

  @Test
  public void searchShouldReturnHistoryDtosWithResolvedUsernames() {
    UUID userA = randomUUID();
    UUID userB = randomUUID();

    StockEvent event1 = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ISSUE).withDocumentNumber(DOC_1).build();
    event1.setUserId(userA);
    StockEvent event2 = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.RECEIVE).withDocumentNumber(DOC_2).build();
    event2.setUserId(userB);

    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(asList(event1, event2), pageable, 2));
    when(userReferenceDataService.findUsersByIds(anySet()))
        .thenReturn(asList(userDto(userA, ALICE), userDto(userB, "bob")));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    assertThat(result.getTotalElements(), is(2L));
    assertThat(result.getContent(), hasSize(2));
    assertThat(result.getContent().get(0).getDocumentNumber(), is(DOC_1));
    assertThat(result.getContent().get(0).getUsername(), is(ALICE));
    assertThat(result.getContent().get(1).getDocumentNumber(), is(DOC_2));
    assertThat(result.getContent().get(1).getUsername(), is("bob"));
  }

  @Test
  public void searchShouldPopulateEntriesCountAndOccurredDateFromAggregate() {
    StockEvent event = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ISSUE).build();
    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        singletonList(EventOrigin.ISSUE), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(singletonList(event), pageable, 1));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(new StockEventLineItemAggregate(
            event.getId(), 3L, 3L, LocalDate.of(2026, Month.FEBRUARY, 10))));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    StockEventHistoryDto dto = result.getContent().get(0);

    assertThat(dto.getEntriesCount(), is(3));
    assertThat(dto.getOccurredDate(), is(LocalDate.of(2026, Month.FEBRUARY, 10)));
  }

  @Test
  public void searchShouldMarkEventReversibleWhenItHasNonCancellationLineItems() {
    StockEvent event = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ADJUSTMENT).build();
    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        singletonList(EventOrigin.ADJUSTMENT), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(singletonList(event), pageable, 1));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(new StockEventLineItemAggregate(
            event.getId(), 4L, 1L, LocalDate.of(2026, Month.FEBRUARY, 10))));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    assertThat(result.getContent().get(0).getReversible(), is(true));
  }

  @Test
  public void searchShouldNotMarkEventReversibleWhenEveryLineItemIsACancellation() {
    StockEvent event = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ADJUSTMENT).build();
    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        singletonList(EventOrigin.ADJUSTMENT), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(singletonList(event), pageable, 1));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(new StockEventLineItemAggregate(
            event.getId(), 3L, 0L, LocalDate.of(2026, Month.FEBRUARY, 10))));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    assertThat(result.getContent().get(0).getReversible(), is(false));
  }

  @Test
  public void searchShouldDefaultEntriesCountToZeroWhenEventHasNoAggregate() {
    StockEvent event = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ISSUE).build();
    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        singletonList(EventOrigin.ISSUE), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(singletonList(event), pageable, 1));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    StockEventHistoryDto dto = result.getContent().get(0);

    assertThat(dto.getEntriesCount(), is(0));
    assertThat(dto.getOccurredDate(), is(nullValue()));
  }

  private UserDto userDto(UUID id, String username) {
    UserDto dto = new UserDto();
    dto.setId(id);
    dto.setUsername(username);
    return dto;
  }

  @Test
  public void findStockEventLineItemsShouldReturnOnlyLinesOfThisEvent() {
    UUID facility = randomUUID();
    UUID program = randomUUID();
    StockEvent event = new StockEventDataBuilder()
        .withFacility(facility).withProgram(program).withEventOrigin(EventOrigin.ISSUE).build();
    UUID eventId = event.getId();
    UUID cardId = randomUUID();

    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockCardLineItemRepository.findStockCardIdsByOriginEvent(eventId))
        .thenReturn(singletonList(cardId));

    StockCardLineItemDto matching = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder()
            .withQuantity(7).withStockOnHand(20).withDocumentNumber("DOC-MATCH").build())
        .originEventId(eventId).build();
    StockCardLineItemDto other = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder()
            .withQuantity(99).withStockOnHand(99).withDocumentNumber("DOC-OTHER").build())
        .originEventId(randomUUID()).build();
    StockCardDto cardDto = StockCardDto.builder()
        .orderable(OrderableDto.builder().productCode(PRODUCT_CODE).build())
        .lineItems(asList(matching, other)).build();
    when(stockCardService.findStockCardsByIds(anyCollection()))
        .thenReturn(singletonList(cardDto));

    Page<StockEventLineDetailDto> result =
        stockEventsService.findStockEventLineItems(eventId, pageable);

    assertThat(result.getContent(), hasSize(1));

    StockEventLineDetailDto detail = result.getContent().get(0);

    assertThat(detail.getQuantity(), is(7));
    assertThat(detail.getStockOnHand(), is(20));
    assertThat(detail.getDocumentNumber(), is("DOC-MATCH"));
    verify(permissionService).canViewStockCard(program, facility);
  }

  @Test
  public void findStockEventLineItemsShouldReturnLinesNewestFirst() {
    UUID facility = randomUUID();
    UUID program = randomUUID();
    StockEvent event = new StockEventDataBuilder()
        .withFacility(facility).withProgram(program).withEventOrigin(EventOrigin.ISSUE).build();
    UUID eventId = event.getId();
    UUID cardId = randomUUID();

    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockCardLineItemRepository.findStockCardIdsByOriginEvent(eventId))
        .thenReturn(singletonList(cardId));

    // Provided in the canonical comparator order (as card.getLineItems() yields them); the
    // service must present them reversed (newest-first) to match the stock card view and
    // stockEvent.jrxml.
    StockCardLineItemDto first = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder().withDocumentNumber(DOC_1).build())
        .originEventId(eventId).build();
    StockCardLineItemDto second = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder().withDocumentNumber(DOC_2).build())
        .originEventId(eventId).build();
    StockCardLineItemDto third = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder().withDocumentNumber(DOC_3).build())
        .originEventId(eventId).build();
    StockCardDto cardDto = StockCardDto.builder()
        .orderable(OrderableDto.builder().productCode(PRODUCT_CODE).build())
        .lineItems(asList(first, second, third)).build();
    when(stockCardService.findStockCardsByIds(anyCollection()))
        .thenReturn(singletonList(cardDto));

    Page<StockEventLineDetailDto> result =
        stockEventsService.findStockEventLineItems(eventId, pageable);

    assertThat(result.getContent(), hasSize(3));
    assertThat(result.getContent().get(0).getDocumentNumber(), is(DOC_3));
    assertThat(result.getContent().get(1).getDocumentNumber(), is(DOC_2));
    assertThat(result.getContent().get(2).getDocumentNumber(), is(DOC_1));
  }

  @Test
  public void findStockEventLineItemsShouldOrderCardsByProductCodeThenLot() {
    StockEvent event = new StockEventDataBuilder()
        .withFacility(randomUUID()).withProgram(randomUUID())
        .withEventOrigin(EventOrigin.ISSUE).build();
    UUID eventId = event.getId();

    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockCardLineItemRepository.findStockCardIdsByOriginEvent(eventId))
        .thenReturn(asList(randomUUID(), randomUUID(), randomUUID()));

    StockCardDto productAnoLot = cardWithLine("AAA", null, eventId, DOC_1);
    StockCardDto productALotL2 = cardWithLine("AAA", "L2", eventId, DOC_2);
    StockCardDto productB = cardWithLine("BBB", null, eventId, DOC_3);
    when(stockCardService.findStockCardsByIds(anyCollection()))
        .thenReturn(asList(productB, productALotL2, productAnoLot));

    Page<StockEventLineDetailDto> result =
        stockEventsService.findStockEventLineItems(eventId, pageable);

    assertThat(result.getContent(), hasSize(3));
    assertThat(result.getContent().get(0).getDocumentNumber(), is(DOC_1));
    assertThat(result.getContent().get(1).getDocumentNumber(), is(DOC_2));
    assertThat(result.getContent().get(2).getDocumentNumber(), is(DOC_3));
  }

  @Test
  public void findStockEventLineItemsShouldTellForEachRowWhetherItCanBeCancelledAndWhy() {
    final StockEventLineItem cancellable = eventLineItem();
    final StockEventLineItem blocked = eventLineItem();
    final StockEvent event = eventWithLineItems(cancellable, blocked);
    final UUID eventId = event.getId();
    StockCardDto cardDto = StockCardDto.builder()
        .orderable(OrderableDto.builder().productCode(PRODUCT_CODE).build())
        .lineItems(asList(row(eventId, cancellable.getId(), DOC_1),
            row(eventId, blocked.getId(), DOC_2),
            row(eventId, null, DOC_3)))
        .build();
    when(stockCardService.findStockCardsByIds(anyCollection()))
        .thenReturn(singletonList(cardDto));
    BlockingTransactionDto inventory =
        new BlockingTransactionDto("PHYSICAL_INVENTORY", LocalDate.now(), "PI-1");
    when(cancelValidationService.evaluate(eq(event), anyCollection()))
        .thenReturn(singletonMap(blocked.getId(), new StockEventCancellationLineErrorDto(
            blocked.getId(), ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, null,
            singletonList(inventory))));

    Map<String, StockEventLineDetailDto> rows =
        stockEventsService.findStockEventLineItems(eventId, pageable).getContent().stream()
            .collect(toMap(StockEventLineDetailDto::getDocumentNumber, identity()));

    verify(cancelValidationService).evaluate(eq(event), evaluatedCaptor.capture());
    assertThat(evaluatedCaptor.getValue(), containsInAnyOrder(cancellable, blocked));
    assertThat(rows.get(DOC_1).getCancellable(), is(true));
    assertThat(rows.get(DOC_1).getCancellationError(), is(nullValue()));
    StockEventCancellationLineErrorDto blockedError = rows.get(DOC_2).getCancellationError();
    assertThat(rows.get(DOC_2).getCancellable(), is(false));
    assertThat(blockedError.getMessageKey(), is(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY));
    assertThat(blockedError.getMessage(), is(LOCALIZED));
    assertThat(blockedError.getBlockingTransactions(), contains(inventory));
    // recorded before line items were linked: never selectable for cancellation
    StockEventCancellationLineErrorDto legacyError = rows.get(DOC_3).getCancellationError();
    assertThat(rows.get(DOC_3).getCancellable(), is(false));
    assertThat(legacyError.getMessageKey(), is(ERROR_EVENT_LINE_ITEM_RECORDED_BEFORE_REVERSAL));
    assertThat(legacyError.getMessage(), is(LOCALIZED));
  }

  @Test
  public void findStockEventLineItemsShouldCheckCancellationOnlyForRowsOfTheRequestedPage() {
    final StockEventLineItem first = eventLineItem();
    final StockEventLineItem second = eventLineItem();
    final StockEventLineItem third = eventLineItem();
    final StockEvent event = eventWithLineItems(first, second, third);
    final UUID eventId = event.getId();
    StockCardDto cardDto = StockCardDto.builder()
        .orderable(OrderableDto.builder().productCode(PRODUCT_CODE).build())
        .lineItems(asList(row(eventId, first.getId(), DOC_1),
            row(eventId, second.getId(), DOC_2),
            row(eventId, third.getId(), DOC_3)))
        .build();
    when(stockCardService.findStockCardsByIds(anyCollection()))
        .thenReturn(singletonList(cardDto));

    Page<StockEventLineDetailDto> result =
        stockEventsService.findStockEventLineItems(eventId, PageRequest.of(0, 1));

    verify(cancelValidationService).evaluate(eq(event), evaluatedCaptor.capture());
    assertThat(evaluatedCaptor.getValue(), hasSize(1));
    assertThat(evaluatedCaptor.getValue().iterator().next().getId(),
        is(result.getContent().get(0).getStockEventLineItemId()));
    assertThat(result.getContent().get(0).getCancellable(), is(true));
  }

  private StockEvent eventWithLineItems(StockEventLineItem... lineItems) {
    StockEvent event = new StockEventDataBuilder()
        .withFacility(randomUUID()).withProgram(randomUUID())
        .withEventOrigin(EventOrigin.ISSUE).withLineItems(asList(lineItems)).build();
    when(stockEventsRepository.findById(event.getId())).thenReturn(Optional.of(event));
    when(stockCardLineItemRepository.findStockCardIdsByOriginEvent(event.getId()))
        .thenReturn(singletonList(randomUUID()));
    return event;
  }

  private StockEventLineItem eventLineItem() {
    StockEventLineItem lineItem = new StockEventLineItem();
    lineItem.setId(randomUUID());
    return lineItem;
  }

  private StockCardLineItemDto row(UUID eventId, UUID stockEventLineItemId,
      String documentNumber) {
    return StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder().withDocumentNumber(documentNumber).build())
        .originEventId(eventId)
        .stockEventLineItemId(stockEventLineItemId)
        .build();
  }

  private StockCardDto cardWithLine(String productCode, String lotCode, UUID eventId,
      String documentNumber) {
    StockCardLineItemDto line = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder().withDocumentNumber(documentNumber).build())
        .originEventId(eventId).build();
    return StockCardDto.builder()
        .id(randomUUID())
        .orderable(OrderableDto.builder().productCode(productCode).build())
        .lot(lotCode == null ? null : LotDto.builder().lotCode(lotCode).build())
        .lineItems(singletonList(line)).build();
  }

  @Test
  public void findStockEventLineItemsShouldCarryFreeTextsOfTheLineItem() {
    UUID facility = randomUUID();
    UUID program = randomUUID();
    StockEvent event = new StockEventDataBuilder()
        .withFacility(facility).withProgram(program).withEventOrigin(EventOrigin.ISSUE).build();
    UUID eventId = event.getId();
    UUID cardId = randomUUID();

    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockCardLineItemRepository.findStockCardIdsByOriginEvent(eventId))
        .thenReturn(singletonList(cardId));

    StockCardLineItemDto line = StockCardLineItemDto.builder()
        .lineItem(new StockCardLineItemDataBuilder()
            .withReasonFreeText("damaged in transit")
            .withSourceFreeText("other warehouse")
            .withDestinationFreeText("other clinic")
            .build())
        .originEventId(eventId).build();
    StockCardDto cardDto = StockCardDto.builder()
        .orderable(OrderableDto.builder().productCode(PRODUCT_CODE).build())
        .lineItems(singletonList(line)).build();
    when(stockCardService.findStockCardsByIds(anyCollection()))
        .thenReturn(singletonList(cardDto));

    Page<StockEventLineDetailDto> result =
        stockEventsService.findStockEventLineItems(eventId, pageable);

    StockEventLineDetailDto detail = result.getContent().get(0);

    assertThat(detail.getReasonFreeText(), is("damaged in transit"));
    assertThat(detail.getSourceFreeText(), is("other warehouse"));
    assertThat(detail.getDestinationFreeText(), is("other clinic"));
  }

  @Test(expected = ResourceNotFoundException.class)
  public void findStockEventLineItemsShouldThrowWhenEventNotFound() {
    UUID eventId = randomUUID();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.empty());

    stockEventsService.findStockEventLineItems(eventId, pageable);
  }

  @Test
  public void findStockEventShouldReturnHistoryHeaderForEvent() {
    UUID facility = randomUUID();
    UUID program = randomUUID();
    UUID user = randomUUID();
    StockEvent event = new StockEventDataBuilder()
        .withFacility(facility).withProgram(program)
        .withEventOrigin(EventOrigin.RECEIVE).withDocumentNumber(DOC_1)
        .withSignature("signature-user").build();
    event.setUserId(user);
    UUID eventId = event.getId();

    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(
            new StockEventLineItemAggregate(
            eventId, 3L, 3L, LocalDate.of(2026, Month.FEBRUARY, 10))));
    when(userReferenceDataService.findUsersByIds(anySet()))
        .thenReturn(singletonList(userDto(user, ALICE)));

    StockEventHistoryDto dto = stockEventsService.findStockEvent(eventId);

    assertThat(dto.getId(), is(eventId));
    assertThat(dto.getType(), is(EventOrigin.RECEIVE));
    assertThat(dto.getDocumentNumber(), is(DOC_1));
    assertThat(dto.getSignature(), is("signature-user"));
    assertThat(dto.getEntriesCount(), is(3));
    assertThat(dto.getOccurredDate(), is(LocalDate.of(2026, Month.FEBRUARY, 10)));
    assertThat(dto.getProcessedDate(), is(event.getProcessedDate()));
    assertThat(dto.getUsername(), is(ALICE));
    // the detail view resolves the cancel permission and the current stock on hand from these
    assertThat(dto.getFacilityId(), is(facility));
    assertThat(dto.getProgramId(), is(program));
    verify(permissionService).canViewStockCard(program, facility);
  }

  @Test
  public void findStockEventShouldBeReversibleOnlyWhenALinkedLineItemCanBeCancelled() {
    StockEvent event = new StockEventDataBuilder().withEventOrigin(EventOrigin.ISSUE).build();
    UUID eventId = event.getId();
    UUID linkedLineItemId = randomUUID();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(new StockEventLineItemAggregate(
            eventId, 2L, 2L, LocalDate.of(2026, Month.FEBRUARY, 10))));
    when(stockCardLineItemRepository.findOriginEventLineItemIdsByOriginEvent(eventId))
        .thenReturn(singletonList(linkedLineItemId));
    when(cancelValidationService.hasCancellableLine(event, singleton(linkedLineItemId)))
        .thenReturn(true, false);

    assertThat(stockEventsService.findStockEvent(eventId).getReversible(), is(true));
    assertThat(stockEventsService.findStockEvent(eventId).getReversible(), is(false));
  }

  @Test
  public void findStockEventShouldNotCheckCancellationRulesWhenEventHasNoOrigin() {
    // pushed by another service without an origin, e.g. requisition adjustments on approval
    StockEvent event = new StockEventDataBuilder().build();
    event.setEventOrigin(null);
    UUID eventId = event.getId();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(new StockEventLineItemAggregate(
            eventId, 2L, 2L, LocalDate.of(2026, Month.FEBRUARY, 10))));

    assertThat(stockEventsService.findStockEvent(eventId).getReversible(), is(false));
    verify(stockCardLineItemRepository, never()).findOriginEventLineItemIdsByOriginEvent(any());
    verifyNoInteractions(cancelValidationService);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void findStockEventShouldThrowWhenEventNotFound() {
    UUID eventId = randomUUID();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.empty());

    stockEventsService.findStockEvent(eventId);
  }
}
