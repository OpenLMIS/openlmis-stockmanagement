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
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class CalculatedStockOnHandRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<CalculatedStockOnHand> {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Override
  CrudRepository<CalculatedStockOnHand, UUID> getRepository() {
    return calculatedStockOnHandRepository;
  }

  @Override
  CalculatedStockOnHand generateInstance() {
    return generateInstance(randomUUID(), randomUUID(), randomUUID(), randomUUID());
  }

  private CalculatedStockOnHand generateInstance(UUID facility, UUID program, UUID product,
                                                 UUID lot) {
    StockEvent event = new StockEventDataBuilder()
            .withoutId()
            .withFacility(facility)
            .withProgram(program)
            .build();

    event = stockEventsRepository.save(event);

    StockCard stockCard = new StockCardDataBuilder(event)
            .withoutId()
            .withOrderable(product)
            .withLot(lot)
            .build();

    stockCard = stockCardRepository.save(stockCard);

    return new CalculatedStockOnHandDataBuilder()
        .withoutId()
        .withStockCard(stockCard)
        .build();
  }

  @Test
  public void shouldReturnCalculatedStockOnHandsWhenNoStartDateProvided() {
    CalculatedStockOnHand calculatedStockOnHand1 = this.generateInstance();
    calculatedStockOnHand1.setOccurredDate(LocalDate.of(2010, 8, 1));
    calculatedStockOnHandRepository.save(calculatedStockOnHand1);

    CalculatedStockOnHand calculatedStockOnHand2 = this.generateInstance();
    calculatedStockOnHand2.setOccurredDate(LocalDate.of(2010, 9, 1));
    calculatedStockOnHandRepository.save(calculatedStockOnHand2);

    CalculatedStockOnHand calculatedStockOnHand3 = this.generateInstance();
    calculatedStockOnHand3.setOccurredDate(LocalDate.of(2010, 10, 1));
    calculatedStockOnHandRepository.save(calculatedStockOnHand3);

    List<CalculatedStockOnHand> resultList1 = calculatedStockOnHandRepository
            .findByStockCardIdInAndOccurredDateLessThanEqual(
                    asList(calculatedStockOnHand1.getStockCard().getId(),
                    calculatedStockOnHand2.getStockCard().getId(),
                    calculatedStockOnHand3.getStockCard().getId()),
                    LocalDate.of(2010, 11, 1));

    assertThat(resultList1, hasItems(calculatedStockOnHand1,
            calculatedStockOnHand2, calculatedStockOnHand3));
    assertEquals(resultList1.size(), 3);

    List<CalculatedStockOnHand> resultList2 = calculatedStockOnHandRepository
            .findByStockCardIdInAndOccurredDateLessThanEqual(
                    asList(calculatedStockOnHand1.getStockCard().getId(),
                            calculatedStockOnHand2.getStockCard().getId(),
                            calculatedStockOnHand3.getStockCard().getId()),
                    LocalDate.of(2010, 9, 15));

    assertThat(resultList2, hasItems(calculatedStockOnHand1, calculatedStockOnHand2));
    assertEquals(resultList2.size(), 2);

    List<CalculatedStockOnHand> resultList3 = calculatedStockOnHandRepository
            .findByStockCardIdInAndOccurredDateLessThanEqual(
                    asList(calculatedStockOnHand1.getStockCard().getId(),
                            calculatedStockOnHand2.getStockCard().getId(),
                            calculatedStockOnHand3.getStockCard().getId()),
                    LocalDate.of(2010, 8, 15));

    assertThat(resultList3, hasItems(calculatedStockOnHand1));
    assertEquals(resultList3.size(), 1);

    List<CalculatedStockOnHand> resultList4 = calculatedStockOnHandRepository
            .findByStockCardIdInAndOccurredDateLessThanEqual(
                    asList(calculatedStockOnHand1.getStockCard().getId(),
                            calculatedStockOnHand2.getStockCard().getId(),
                            calculatedStockOnHand3.getStockCard().getId()),
                    LocalDate.of(2010, 4, 15));

    assertTrue(resultList4.isEmpty());
    assertEquals(resultList4.size(), 0);
  }
}
