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

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalculatedStockOnHandServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private CalculatedStockOnHandService calculatedStockOnHandService;

  @Test
  public void shouldSaveCalculatedStockOnHandFromStockCard() {
    //given
    int quantity = 20;
    StockCard stockCard = getStockCard(randomUUID(), randomUUID(), randomUUID(), randomUUID(),
            quantity);

    //when
    CalculatedStockOnHand result = calculatedStockOnHandService.saveFromStockCard(stockCard);

    //then
    assertThat(result.getStockOnHand(), is(quantity));
  }

  private StockCard getStockCard(UUID facility, UUID program, UUID product, UUID lot,
                                 Integer quantity) {
    StockEvent event = new StockEventDataBuilder()
            .withoutId()
            .withFacility(facility)
            .withProgram(program)
            .build();

    StockCardLineItem lineItem = StockCardLineItem.builder()
            .quantity(quantity)
            .occurredDate(LocalDate.now())
            .build();

    return new StockCardDataBuilder(event)
            .withoutId()
            .withOrderable(product)
            .withLot(lot)
            .withLineItem(lineItem)
            .build();
  }
}
