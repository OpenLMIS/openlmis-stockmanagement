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
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.repository.custom.SubmittedPhysicalInventoryLine;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class PhysicalInventoriesRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<PhysicalInventory> {

  private static final LocalDate MOVEMENT_DATE = LocalDate.of(2026, 1, 1);
  private static final ZonedDateTime MOVEMENT_PROCESSED =
      ZonedDateTime.parse("2026-01-01T10:00:00Z");

  @Autowired
  private PhysicalInventoriesRepository repository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Override
  CrudRepository<PhysicalInventory, UUID> getRepository() {
    return repository;
  }

  @Override
  PhysicalInventory generateInstance() {
    return buildInventory(randomUUID(), randomUUID(), randomUUID(), randomUUID(),
        MOVEMENT_DATE, false);
  }

  @Test
  public void shouldReturnSubmittedLinesOfTheGivenOrderablesFromTheGivenDate() {
    UUID program = randomUUID();
    UUID facility = randomUUID();
    UUID orderable = randomUUID();
    UUID otherOrderable = randomUUID();
    UUID lot = randomUUID();
    final PhysicalInventory sameDayWithoutLot =
        save(program, facility, orderable, null, MOVEMENT_DATE, false);
    final PhysicalInventory nextDay =
        save(program, facility, orderable, lot, MOVEMENT_DATE.plusDays(1), false);
    final PhysicalInventory otherOrderableLater =
        save(program, facility, otherOrderable, lot, MOVEMENT_DATE.plusDays(2), false);
    save(program, facility, orderable, lot, MOVEMENT_DATE.plusDays(1), true); // draft
    save(program, facility, orderable, lot, MOVEMENT_DATE.minusDays(1), false); // earlier
    save(program, facility, randomUUID(), lot, MOVEMENT_DATE.plusDays(1), false); // not asked for
    save(randomUUID(), facility, orderable, lot, MOVEMENT_DATE.plusDays(1), false); // program
    save(program, randomUUID(), orderable, lot, MOVEMENT_DATE.plusDays(1), false); // facility

    List<SubmittedPhysicalInventoryLine> result = repository.findSubmittedLinesOfOrderablesFrom(
        program, facility, asList(orderable, otherOrderable), MOVEMENT_DATE);

    assertThat(result.stream().map(SubmittedPhysicalInventoryLine::getPhysicalInventoryId)
            .collect(toList()),
        contains(sameDayWithoutLot.getId(), nextDay.getId(), otherOrderableLater.getId()));
    assertThat(result.get(0).getOrderableId(), is(orderable));
    assertThat(result.get(0).getLotId(), is(nullValue()));
    assertThat(result.get(1).getLotId(), is(lot));
    assertThat(result.get(2).getOrderableId(), is(otherOrderable));
  }

  @Test
  public void shouldReturnProcessedDateOfTheInventoryStockEvent() {
    UUID program = randomUUID();
    UUID facility = randomUUID();
    UUID orderable = randomUUID();
    UUID lot = randomUUID();
    // Same occurred date as the movement: the caller orders such inventories by this processed
    // date, so it must come back as the same instant that was stored.
    final PhysicalInventory withEvent = saveWithEvent(program, facility, orderable, lot,
        MOVEMENT_DATE, MOVEMENT_PROCESSED.plusHours(1));
    final PhysicalInventory withoutEvent =
        save(program, facility, orderable, lot, MOVEMENT_DATE, false);

    Map<UUID, SubmittedPhysicalInventoryLine> result = repository
        .findSubmittedLinesOfOrderablesFrom(program, facility, singletonList(orderable),
            MOVEMENT_DATE)
        .stream()
        .collect(toMap(SubmittedPhysicalInventoryLine::getPhysicalInventoryId, identity()));

    assertThat(result.get(withEvent.getId()).getProcessedDate().toInstant(),
        is(MOVEMENT_PROCESSED.plusHours(1).toInstant()));
    assertThat(result.get(withoutEvent.getId()).getProcessedDate(), is(nullValue()));
  }

  private PhysicalInventory save(UUID program, UUID facility, UUID orderable, UUID lot,
      LocalDate occurredDate, boolean draft) {
    return repository.save(buildInventory(program, facility, orderable, lot, occurredDate, draft));
  }

  // Saves a submitted inventory linked to a stock event with the given processed date, so the
  // occurred-date tiebreaker (StockCard ordering) can be exercised.
  private PhysicalInventory saveWithEvent(UUID program, UUID facility, UUID orderable, UUID lot,
      LocalDate occurredDate, ZonedDateTime processedDate) {
    StockEvent event = stockEventsRepository.save(new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .withProcessedDate(processedDate)
        .build());
    PhysicalInventory inventory = buildInventory(program, facility, orderable, lot, occurredDate,
        false);
    inventory.setStockEvent(event);
    return repository.save(inventory);
  }

  private PhysicalInventory buildInventory(UUID program, UUID facility, UUID orderable, UUID lot,
      LocalDate occurredDate, boolean draft) {
    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setProgramId(program);
    inventory.setFacilityId(facility);
    inventory.setIsDraft(draft);
    inventory.setOccurredDate(occurredDate);

    PhysicalInventoryLineItem lineItem = new PhysicalInventoryLineItem();
    lineItem.setOrderableId(orderable);
    lineItem.setLotId(lot);
    lineItem.setPhysicalInventory(inventory);
    inventory.setLineItems(singletonList(lineItem));

    return inventory;
  }
}
