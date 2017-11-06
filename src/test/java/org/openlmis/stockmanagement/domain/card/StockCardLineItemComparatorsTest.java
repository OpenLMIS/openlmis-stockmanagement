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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byOccurredDate;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byProcessedDate;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byReasonPriority;

import org.junit.Test;
import org.openlmis.stockmanagement.testutils.StockCardLineItemBuilder;

public class StockCardLineItemComparatorsTest {

  @Test
  public void shouldSortByOccurredDate() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemBuilder().build();
    StockCardLineItem right = new StockCardLineItemBuilder().build();

    // then
    assertThat(byOccurredDate().compare(left, right), is(0));

    // when
    right = new StockCardLineItemBuilder().withOccurredDateNextDay().build();

    // then
    assertThat(byOccurredDate().compare(left, right), is(-1));

    // when
    right = new StockCardLineItemBuilder().withOccurredDatePreviousDay().build();

    // then
    assertThat(byOccurredDate().compare(left, right), is(1));
  }

  @Test
  public void shouldSortByProcessedDate() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemBuilder().build();
    StockCardLineItem right = new StockCardLineItemBuilder().build();

    // then
    assertThat(byProcessedDate().compare(left, right), is(0));

    // when
    right = new StockCardLineItemBuilder().withProcessedDateNextDay().build();

    // then
    assertThat(byProcessedDate().compare(left, right), is(-1));

    // when
    right = new StockCardLineItemBuilder().withProcessedDateHourEarlier().build();

    // then
    assertThat(byProcessedDate().compare(left, right), is(1));
  }

  @Test
  public void shouldSortByReasonPriority() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemBuilder().withCreditReason().build();
    StockCardLineItem right = new StockCardLineItemBuilder().withCreditReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(0));

    // when
    left = new StockCardLineItemBuilder().withCreditReason().build();
    right = new StockCardLineItemBuilder().withDebitReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(-1));

    // when
    left = new StockCardLineItemBuilder().withDebitReason().build();
    right = new StockCardLineItemBuilder().withCreditReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(1));
  }

  @Test
  public void shouldSortByReasonPriorityIfReasonIsNull() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemBuilder().build();
    StockCardLineItem right = new StockCardLineItemBuilder().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(0));

    // when
    left = new StockCardLineItemBuilder().build();
    right = new StockCardLineItemBuilder().withCreditReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(1));

    // when
    left = new StockCardLineItemBuilder().withCreditReason().build();
    right = new StockCardLineItemBuilder().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(-1));
  }
}