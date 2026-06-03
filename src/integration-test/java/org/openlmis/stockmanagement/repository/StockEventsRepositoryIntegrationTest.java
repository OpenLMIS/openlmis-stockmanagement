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

package org.openlmis.stockmanagement.repository;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.repository.custom.StockEventLineItemAggregate;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings("PMD.TooManyMethods")
public class StockEventsRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<StockEvent> {

  @Autowired
  private StockEventsRepository repository;

  private Pageable pageable = PageRequest.of(0, 10);

  @Override
  CrudRepository<StockEvent, UUID> getRepository() {
    return repository;
  }

  @Override
  StockEvent generateInstance() {
    return new StockEventDataBuilder()
        .withoutId()
        .withEventOrigin(EventOrigin.ISSUE)
        .build();
  }

  @Test
  public void shouldReturnOnlyIssueAndReceiveEventsForGivenFacilityAndProgram() {
    UUID facility = randomUUID();
    UUID program = randomUUID();

    final StockEvent issue = save(facility, program, EventOrigin.ISSUE);
    final StockEvent receive = save(facility, program, EventOrigin.RECEIVE);
    save(facility, program, null);                  // legacy event (null origin) - excluded
    save(randomUUID(), program, EventOrigin.ISSUE);  // other facility - excluded
    save(facility, randomUUID(), EventOrigin.ISSUE); // other program - excluded

    StockEventSearchParams params = new StockEventSearchParams(facility, program,
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE), null, null, null);

    Page<StockEvent> result = repository.search(params, pageable);

    assertThat(result.getContent(), hasSize(2));
    assertThat(ids(result), containsInAnyOrder(issue.getId(), receive.getId()));
  }

  @Test
  public void shouldReturnEventsOrderedByProcessedDateDescending() {
    UUID facility = randomUUID();
    UUID program = randomUUID();

    StockEvent older = saveWithDate(facility, program, ZonedDateTime.parse("2026-01-01T10:00:00Z"));
    StockEvent newer = saveWithDate(facility, program, ZonedDateTime.parse("2026-03-01T10:00:00Z"));
    StockEvent middle =
        saveWithDate(facility, program, ZonedDateTime.parse("2026-02-01T10:00:00Z"));

    StockEventSearchParams params = new StockEventSearchParams(facility, program,
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE), null, null, null);

    Page<StockEvent> result = repository.search(params, pageable);

    assertThat(ids(result), contains(newer.getId(), middle.getId(), older.getId()));
  }

  @Test
  public void shouldFilterByDocumentNumberSubstring() {
    UUID facility = randomUUID();
    UUID program = randomUUID();

    StockEvent matching = saveWithDocumentNumber(facility, program, "2026-06-FAC001-0001");
    saveWithDocumentNumber(facility, program, "2026-06-FAC001-0002");

    StockEventSearchParams params = new StockEventSearchParams(facility, program,
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE), null, null, "0001");

    Page<StockEvent> result = repository.search(params, pageable);

    assertThat(result.getContent(), hasSize(1));
    assertThat(ids(result), contains(matching.getId()));
  }

  @Test
  public void shouldFilterByOccurredDateRange() {
    UUID facility = randomUUID();
    UUID program = randomUUID();

    saveWithOccurredDate(facility, program, LocalDate.of(2026, 1, 15));
    StockEvent february = saveWithOccurredDate(facility, program, LocalDate.of(2026, 2, 15));
    saveWithOccurredDate(facility, program, LocalDate.of(2026, 3, 15));

    StockEventSearchParams params = new StockEventSearchParams(facility, program,
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE),
        LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), null);

    Page<StockEvent> result = repository.search(params, pageable);

    assertThat(result.getContent(), hasSize(1));
    assertThat(ids(result), contains(february.getId()));
  }

  @Test
  public void shouldNotDuplicateEventWithMultipleLineItemsInsideDateRange() {
    UUID facility = randomUUID();
    UUID program = randomUUID();

    StockEvent event = new StockEventDataBuilder()
        .withoutId().withFacility(facility).withProgram(program)
        .withEventOrigin(EventOrigin.ISSUE).build();
    event.setLineItems(asList(
        lineItem(event, randomUUID(), LocalDate.of(2026, 2, 10)),
        lineItem(event, randomUUID(), LocalDate.of(2026, 2, 20))));
    repository.save(event);

    StockEventSearchParams params = new StockEventSearchParams(facility, program,
        asList(EventOrigin.ISSUE, EventOrigin.RECEIVE),
        LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), null);

    Page<StockEvent> result = repository.search(params, pageable);

    assertThat(result.getContent(), hasSize(1));
    assertThat(result.getTotalElements(), is(1L));
  }

  @Test
  public void shouldAggregateDistinctProductsAndEarliestOccurredDate() {
    UUID facility = randomUUID();
    UUID program = randomUUID();
    UUID orderableA = randomUUID();

    StockEvent event = new StockEventDataBuilder()
        .withoutId().withFacility(facility).withProgram(program)
        .withEventOrigin(EventOrigin.ISSUE).build();
    event.setLineItems(asList(
        lineItem(event, orderableA, LocalDate.of(2026, 2, 20)),
        lineItem(event, orderableA, LocalDate.of(2026, 2, 10)),
        lineItem(event, randomUUID(), LocalDate.of(2026, 2, 15))));
    repository.save(event);

    List<StockEventLineItemAggregate> rows =
        repository.aggregateLineItemsByEventIds(singletonList(event.getId()));

    assertThat(rows, hasSize(1));

    StockEventLineItemAggregate row = rows.get(0);

    assertThat(row.getStockEventId(), is(event.getId()));
    assertThat(row.getNumberOfProducts(), is(2));
    assertThat(row.getOccurredDate(), is(LocalDate.of(2026, 2, 10)));
  }

  @Test
  public void aggregateShouldReturnNoRowForEventWithoutLineItems() {
    UUID facility = randomUUID();
    UUID program = randomUUID();
    StockEvent event = save(facility, program, EventOrigin.ISSUE);

    List<StockEventLineItemAggregate> rows =
        repository.aggregateLineItemsByEventIds(singletonList(event.getId()));

    assertThat(rows, hasSize(0));
  }

  private StockEventLineItem lineItem(StockEvent event, UUID orderableId, LocalDate occurredDate) {
    return StockEventLineItem.builder()
        .orderableId(orderableId)
        .quantity(10)
        .occurredDate(occurredDate)
        .stockEvent(event)
        .build();
  }

  private StockEvent save(UUID facility, UUID program, EventOrigin origin) {
    return repository.save(new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .withEventOrigin(origin)
        .build());
  }

  private StockEvent saveWithDate(UUID facility, UUID program, ZonedDateTime processedDate) {
    return repository.save(new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .withEventOrigin(EventOrigin.ISSUE)
        .withProcessedDate(processedDate)
        .build());
  }

  private StockEvent saveWithDocumentNumber(UUID facility, UUID program, String documentNumber) {
    return repository.save(new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .withEventOrigin(EventOrigin.ISSUE)
        .withDocumentNumber(documentNumber)
        .build());
  }

  private StockEvent saveWithOccurredDate(UUID facility, UUID program, LocalDate occurredDate) {
    StockEvent event = new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .withEventOrigin(EventOrigin.ISSUE)
        .build();
    StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(randomUUID())
        .quantity(10)
        .occurredDate(occurredDate)
        .stockEvent(event)
        .build();
    event.setLineItems(singletonList(lineItem));

    return repository.save(event);
  }

  private List<UUID> ids(Page<StockEvent> page) {
    return page.getContent().stream().map(StockEvent::getId).collect(Collectors.toList());
  }
}
