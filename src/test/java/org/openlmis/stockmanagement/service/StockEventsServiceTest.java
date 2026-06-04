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
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventHistoryDto;
import org.openlmis.stockmanagement.dto.StockEventLineDetailDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.repository.custom.StockEventLineItemAggregate;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class StockEventsServiceTest {

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

  @InjectMocks
  private StockEventsService stockEventsService;

  private Pageable pageable = PageRequest.of(0, 10);

  @Test
  public void searchShouldReturnHistoryDtosWithResolvedUsernames() {
    UUID userA = randomUUID();
    UUID userB = randomUUID();

    StockEvent event1 = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ISSUE).withDocumentNumber("DOC-1").build();
    event1.setUserId(userA);
    StockEvent event2 = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.RECEIVE).withDocumentNumber("DOC-2").build();
    event2.setUserId(userB);

    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(asList(event1, event2), pageable, 2));
    when(userReferenceDataService.findUsersByIds(anySet()))
        .thenReturn(asList(userDto(userA, "alice"), userDto(userB, "bob")));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    assertThat(result.getTotalElements(), is(2L));
    assertThat(result.getContent(), hasSize(2));
    assertThat(result.getContent().get(0).getDocumentNumber(), is("DOC-1"));
    assertThat(result.getContent().get(0).getUsername(), is("alice"));
    assertThat(result.getContent().get(1).getDocumentNumber(), is("DOC-2"));
    assertThat(result.getContent().get(1).getUsername(), is("bob"));
  }

  @Test
  public void searchShouldPopulateNumberOfProductsAndOccurredDateFromAggregate() {
    StockEvent event = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ISSUE).build();
    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        singletonList(EventOrigin.ISSUE), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(singletonList(event), pageable, 1));
    when(stockEventsRepository.aggregateLineItemsByEventIds(anySet()))
        .thenReturn(singletonList(
            new StockEventLineItemAggregate(event.getId(), 3L, LocalDate.of(2026, 2, 10))));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    StockEventHistoryDto dto = result.getContent().get(0);

    assertThat(dto.getNumberOfProducts(), is(3));
    assertThat(dto.getOccurredDate(), is(LocalDate.of(2026, 2, 10)));
  }

  @Test
  public void searchShouldDefaultNumberOfProductsToZeroWhenEventHasNoAggregate() {
    StockEvent event = new StockEventDataBuilder()
        .withEventOrigin(EventOrigin.ISSUE).build();
    StockEventSearchParams params = new StockEventSearchParams(randomUUID(), randomUUID(),
        singletonList(EventOrigin.ISSUE), null, null, null);

    when(stockEventsRepository.search(params, pageable))
        .thenReturn(new PageImpl<>(singletonList(event), pageable, 1));

    Page<StockEventHistoryDto> result = stockEventsService.search(params, pageable);

    StockEventHistoryDto dto = result.getContent().get(0);

    assertThat(dto.getNumberOfProducts(), is(0));
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
        .orderable(OrderableDto.builder().productCode("ABC").build())
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

  @Test(expected = ResourceNotFoundException.class)
  public void findStockEventLineItemsShouldThrowWhenEventNotFound() {
    UUID eventId = randomUUID();
    when(stockEventsRepository.findById(eventId)).thenReturn(Optional.empty());

    stockEventsService.findStockEventLineItems(eventId, pageable);
  }
}
