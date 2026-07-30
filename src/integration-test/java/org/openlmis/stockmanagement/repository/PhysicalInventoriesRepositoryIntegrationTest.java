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

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
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
  public void shouldReturnSubmittedInventoryRecordedAfterMovementForOrderableAndLot() {
    UUID program = randomUUID();
    UUID facility = randomUUID();
    UUID orderable = randomUUID();
    UUID lot = randomUUID();
    final PhysicalInventory expected =
        save(program, facility, orderable, lot, MOVEMENT_DATE.plusDays(1), false);
    save(program, facility, orderable, lot, MOVEMENT_DATE.plusDays(1), true); // draft
    save(program, facility, orderable, lot, MOVEMENT_DATE.minusDays(1), false); // earlier
    save(program, facility, randomUUID(), lot, MOVEMENT_DATE.plusDays(1), false); // other product
    save(program, facility, orderable, null, MOVEMENT_DATE.plusDays(1), false); // no lot

    List<PhysicalInventory> result = repository.findSubmittedAfterForOrderableAndLot(
        program, facility, orderable, lot, MOVEMENT_DATE, MOVEMENT_PROCESSED);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getId(), is(expected.getId()));
  }

  @Test
  public void shouldReturnSameDayInventoryProcessedAfterTheMovementButNotBefore() {
    UUID program = randomUUID();
    UUID facility = randomUUID();
    UUID orderable = randomUUID();
    UUID lot = randomUUID();
    // Same occurred date as the movement: the one processed AFTER it already reflects the movement
    // and must block; the one processed BEFORE must not.
    final PhysicalInventory processedAfter = saveWithEvent(program, facility, orderable, lot,
        MOVEMENT_DATE, MOVEMENT_PROCESSED.plusHours(1));
    saveWithEvent(program, facility, orderable, lot, MOVEMENT_DATE,
        MOVEMENT_PROCESSED.minusHours(1));

    List<PhysicalInventory> result = repository.findSubmittedAfterForOrderableAndLot(
        program, facility, orderable, lot, MOVEMENT_DATE, MOVEMENT_PROCESSED);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getId(), is(processedAfter.getId()));
  }

  @Test
  public void shouldReturnSubmittedInventoryRecordedAfterMovementForOrderableWithoutLot() {
    UUID program = randomUUID();
    UUID facility = randomUUID();
    UUID orderable = randomUUID();
    final PhysicalInventory expected =
        save(program, facility, orderable, null, MOVEMENT_DATE.plusDays(1), false);
    save(program, facility, orderable, null, MOVEMENT_DATE.plusDays(1), true); // draft
    save(program, facility, orderable, null, MOVEMENT_DATE.minusDays(1), false); // earlier
    save(program, facility, orderable, randomUUID(), MOVEMENT_DATE.plusDays(1), false); // lot

    List<PhysicalInventory> result = repository.findSubmittedAfterForOrderableWithoutLot(
        program, facility, orderable, MOVEMENT_DATE, MOVEMENT_PROCESSED);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getId(), is(expected.getId()));
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
