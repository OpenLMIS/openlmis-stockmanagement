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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDateTime;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneDayLater;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneHourEarlier;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneHourLater;

import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;


public class StockCardTest {

  @Test
  public void should_reorder_line_items_by_occurred_then_by_processed_when_calculate_soh()
      throws Exception {
    //given
    ZonedDateTime baseDate = getBaseDateTime();

    StockCardLineItem lineItem1 = new StockCardLineItem();
    lineItem1.setOccurredDate(baseDate);
    lineItem1.setProcessedDate(oneDayLater(baseDate));
    lineItem1.setQuantity(1);

    StockCardLineItem lineItem2 = new StockCardLineItem();
    lineItem2.setOccurredDate(baseDate);
    lineItem2.setProcessedDate(oneHourLater(baseDate));
    lineItem2.setQuantity(1);

    StockCardLineItem lineItem3 = new StockCardLineItem();
    lineItem3.setOccurredDate(oneHourEarlier(baseDate));
    lineItem3.setProcessedDate(oneHourEarlier(baseDate));
    lineItem3.setQuantity(1);

    StockCard stockCard = new StockCard();
    stockCard.setLineItems(asList(lineItem1, lineItem2, lineItem3));

    //when
    stockCard.calculateStockOnHand();

    //then
    assertThat(stockCard.getLineItems().get(0), is(lineItem3));
    assertThat(stockCard.getLineItems().get(1), is(lineItem2));
    assertThat(stockCard.getLineItems().get(2), is(lineItem1));
  }

  @Test
  public void should_get_soh_by_calculating_soh_of_each_line_item_() throws Exception {
    //given
    StockCardLineItem lineItem1 = mock(StockCardLineItem.class);
    StockCardLineItem lineItem2 = mock(StockCardLineItem.class);

    when(lineItem1.getOccurredDate()).thenReturn(getBaseDateTime());
    when(lineItem2.getOccurredDate()).thenReturn(oneDayLater(getBaseDateTime()));
    when(lineItem1.getStockOnHand()).thenReturn(123);
    when(lineItem2.getStockOnHand()).thenReturn(456);

    StockCard card = new StockCard();
    card.setLineItems(asList(lineItem1, lineItem2));

    //when
    card.calculateStockOnHand();

    //then
    verify(lineItem1, Mockito.times(1)).calculateStockOnHand(0);
    verify(lineItem2, Mockito.times(1)).calculateStockOnHand(123);

    assertThat(card.getStockOnHand(), is(456));
  }

  @Test
  public void should_shallow_copy_line_items() throws Exception {
    //given
    StockCard stockCard = new StockCard();

    StockCardLineItem lineItem = new StockCardLineItem();
    lineItem.setQuantity(5);

    stockCard.setLineItems(new ArrayList<>());
    stockCard.getLineItems().add(lineItem);

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