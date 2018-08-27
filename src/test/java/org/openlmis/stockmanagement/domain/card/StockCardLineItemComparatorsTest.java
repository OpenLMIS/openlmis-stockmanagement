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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byOccurredDate;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byProcessedDate;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byReasonPriority;

import org.junit.Test;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
public class StockCardLineItemComparatorsTest {

  @Test
  public void shouldReturnZeroIfOccurredDatesAreSame() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().build();

    // then
    assertThat(byOccurredDate().compare(left, right), is(0));
  }

  @Test
  public void shouldReturnNegativeIfFirstOccurredDateIsEarlier() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().withOccurredDateNextDay().build();

    // then
    assertThat(byOccurredDate().compare(left, right), lessThan(0));
  }

  @Test
  public void shouldReturnPositiveIfFirstOccurredDateIsLater() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder()
        .withOccurredDatePreviousDay()
        .build();

    // then
    assertThat(byOccurredDate().compare(left, right), greaterThan(0));
  }

  @Test
  public void shouldReturnZeroIfProcessedDatesAreSame() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().build();

    // then
    assertThat(byProcessedDate().compare(left, right), is(0));
  }

  @Test
  public void shouldReturnNegativeIfFirstProcessedDateIsEarlier() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().withProcessedDateNextDay().build();

    // then
    assertThat(byProcessedDate().compare(left, right), lessThan(0));
  }

  @Test
  public void shouldReturnPositiveIfFirstProcessedDateIsLater() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder()
        .withProcessedDateHourEarlier()
        .build();

    // then
    assertThat(byProcessedDate().compare(left, right), greaterThan(0));
  }

  @Test
  public void shouldReturnZeroIfReasonPrioritiesAreSame() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().withCreditReason().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().withCreditReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(0));
  }

  @Test
  public void shouldReturnNegativeIfFirstReasonPriorityIsHigher() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().withCreditReason().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().withDebitReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), lessThan(0));
  }

  @Test
  public void shouldReturnPositiveIfFirstReasonPriorityIsLower() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().withDebitReason().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().withCreditReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), greaterThan(0));
  }

  @Test
  public void shouldReturnZeroIfReasonPrioritiesAreNull() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().build();

    // then
    assertThat(byReasonPriority().compare(left, right), is(0));
  }

  @Test
  public void shouldReturnPositiveIfFirstReasonIsNull() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().withCreditReason().build();

    // then
    assertThat(byReasonPriority().compare(left, right), greaterThan(0));
  }

  @Test
  public void shouldReturnPositiveIfSecondReasonIsNull() throws Exception {
    // when
    StockCardLineItem left = new StockCardLineItemDataBuilder().withCreditReason().build();
    StockCardLineItem right = new StockCardLineItemDataBuilder().build();

    // then
    assertThat(byReasonPriority().compare(left, right), lessThan(0));
  }
}