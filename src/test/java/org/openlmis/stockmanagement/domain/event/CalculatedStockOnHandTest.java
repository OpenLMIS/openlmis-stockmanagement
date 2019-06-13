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

package org.openlmis.stockmanagement.domain.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;

import org.junit.Test;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;

public class CalculatedStockOnHandTest {

  @Test
  public void shouldReturnTrueWhenComparingTheSameRecords() {
    //given
    LocalDate date = LocalDate.now();
    CalculatedStockOnHand calculatedStockOnHand1 = new CalculatedStockOnHandDataBuilder().build();
    CalculatedStockOnHand calculatedStockOnHand2 = new CalculatedStockOnHandDataBuilder().build();
    calculatedStockOnHand1.setDate(date);
    calculatedStockOnHand2.setDate(date);

    //when
    boolean result = calculatedStockOnHand1.equals(calculatedStockOnHand2);

    //then
    assertThat(result, is(true));
  }

  @Test
  public void shouldReturnFalseWhenComparingRecordsWithDifferentDate() {
    //given
    LocalDate date = LocalDate.now();
    CalculatedStockOnHand calculatedStockOnHand1 = new CalculatedStockOnHandDataBuilder().build();
    CalculatedStockOnHand calculatedStockOnHand2 = new CalculatedStockOnHandDataBuilder().build();
    calculatedStockOnHand1.setDate(date);
    calculatedStockOnHand2.setDate(date.minusDays(1));

    //when
    boolean result = calculatedStockOnHand1.equals(calculatedStockOnHand2);

    //then
    assertThat(result, is(false));
  }

  @Test
  public void shouldReturnFalseWhenComparingRecordsWithDifferentStockOnHand() {
    //given
    CalculatedStockOnHand calculatedStockOnHand1 = new CalculatedStockOnHandDataBuilder().build();
    CalculatedStockOnHand calculatedStockOnHand2 = new CalculatedStockOnHandDataBuilder().build();
    calculatedStockOnHand1.setStockOnHand(1);
    calculatedStockOnHand2.setStockOnHand(2);

    //when
    boolean result = calculatedStockOnHand1.equals(calculatedStockOnHand2);

    //then
    assertThat(result, is(false));
  }

  @Test
  public void shouldProperlyConvertObjectToString() {
    //given
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHandDataBuilder().build();
    calculatedStockOnHand.setDate(LocalDate.of(2012, 12, 12));

    //when
    String actual = calculatedStockOnHand.toString();
    String expected = "CalculatedStockOnHand"
            + "(stockOnHand=15, stockCard=null, date=2012-12-12)";

    //then
    assertThat(actual, is(expected));
  }
}