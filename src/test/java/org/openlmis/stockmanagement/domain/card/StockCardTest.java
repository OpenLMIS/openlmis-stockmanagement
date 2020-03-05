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

package org.openlmis.stockmanagement.domain.card;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;


public class StockCardTest {

  @Test
  public void shouldReorderLineItemsWhenCalculateSoh() {
    //given
    StockCardLineItem lineItem1 = new StockCardLineItemDataBuilder()
        .withProcessedDateNextDay()
        .withQuantity(10)
        .withDebitReason()
        .build();

    StockCardLineItem lineItem2 = new StockCardLineItemDataBuilder()
        .withProcessedDateNextDay()
        .withQuantity(20)
        .withCreditReason()
        .build();

    StockCardLineItem lineItem3 = new StockCardLineItemDataBuilder()
        .withProcessedDateHourEarlier()
        .withCreditReason()
        .build();

    StockCardLineItem lineItem4 = new StockCardLineItemDataBuilder()
        .withOccurredDatePreviousDay()
        .withProcessedDateHourEarlier()
        .withCreditReason()
        .build();

    StockCard stockCard = StockCard
        .builder()
        .lineItems(asList(lineItem1, lineItem2, lineItem3, lineItem4))
        .build();

    //when
    stockCard.reorderLineItems();

    //then
    assertThat(stockCard.getLineItems().get(0), is(lineItem4));
    assertThat(stockCard.getLineItems().get(1), is(lineItem3));
    assertThat(stockCard.getLineItems().get(2), is(lineItem2));
    assertThat(stockCard.getLineItems().get(3), is(lineItem1));
  }

  @Test
  public void shouldShallowCopyLineItems() {
    //given
    StockCardLineItem lineItem = new StockCardLineItemDataBuilder().withQuantity(5).build();

    StockCard stockCard = new StockCard();
    stockCard.setLineItems(singletonList(lineItem));

    //when
    StockCard copy = stockCard.shallowCopy();

    //then
    assertThat(copy.getLineItems().get(0).getQuantity(), is(5));

    //when
    copy.getLineItems().get(0).setQuantity(6);

    //then
    assertThat(stockCard.getLineItems().get(0).getQuantity(), is(5));
  }
}