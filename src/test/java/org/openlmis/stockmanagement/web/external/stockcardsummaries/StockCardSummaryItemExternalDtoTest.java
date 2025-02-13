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

package org.openlmis.stockmanagement.web.external.stockcardsummaries;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class StockCardSummaryItemExternalDtoTest {
  private static final String TEST_LOT_CODE = "ABC";
  private static final String TEST_ORDERABLE_CODE = "CBA";
  private static final Integer TEST_STOCK_ON_HAND = 7;
  private static final LocalDate TEST_EXPIRATION_DATE = LocalDate.of(2022, 1, 1);
  private static final LocalDate TEST_OCCURRED_DATE = LocalDate.of(2020, 1, 1);

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(StockCardSummaryItemExternalDto.class)
        .suppress(Warning.NONFINAL_FIELDS) // fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    final StockCardSummaryItemExternalDto item = new StockCardSummaryItemExternalDto();
    ToStringTestUtils.verify(StockCardSummaryItemExternalDto.class, item);
  }

  @Test
  public void shouldGet() {
    final StockCardSummaryItemExternalDto item =
        new StockCardSummaryItemExternalDto(TEST_LOT_CODE, TEST_ORDERABLE_CODE, TEST_STOCK_ON_HAND,
            TEST_EXPIRATION_DATE, TEST_OCCURRED_DATE);

    assertEquals(TEST_LOT_CODE, item.getLot());
    assertEquals(TEST_ORDERABLE_CODE, item.getOrderable());
    assertEquals(TEST_STOCK_ON_HAND, item.getStockOnHand());
    assertEquals(TEST_EXPIRATION_DATE, item.getExpirationDate());
    assertEquals(TEST_OCCURRED_DATE, item.getOccurredDate());
  }

  @Test
  public void shouldSet() {
    final StockCardSummaryItemExternalDto item = new StockCardSummaryItemExternalDto();

    item.setLot(TEST_LOT_CODE);
    item.setOrderable(TEST_ORDERABLE_CODE);
    item.setStockOnHand(TEST_STOCK_ON_HAND);
    item.setExpirationDate(TEST_EXPIRATION_DATE);
    item.setOccurredDate(TEST_OCCURRED_DATE);

    assertEquals(TEST_LOT_CODE, item.getLot());
    assertEquals(TEST_ORDERABLE_CODE, item.getOrderable());
    assertEquals(TEST_STOCK_ON_HAND, item.getStockOnHand());
    assertEquals(TEST_EXPIRATION_DATE, item.getExpirationDate());
    assertEquals(TEST_OCCURRED_DATE, item.getOccurredDate());
  }

}
