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

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class StockCardSummaryExternalDtoTest {
  private static final String TEST_PROGRAM_CODE = "ABC";
  private static final String TEST_ORDERABLE_CODE = "CBA";
  private static final Integer TEST_TOTAL_STOCK_ON_HAND = 7;
  private static final List<StockCardSummaryItemExternalDto> TEST_STOCK_CARDS = emptyList();

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(StockCardSummaryExternalDto.class)
        .suppress(Warning.NONFINAL_FIELDS) // fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    final StockCardSummaryExternalDto summary = new StockCardSummaryExternalDto();
    ToStringTestUtils.verify(StockCardSummaryExternalDto.class, summary);
  }

  @Test
  public void shouldGet() {
    final StockCardSummaryExternalDto summary =
        new StockCardSummaryExternalDto(TEST_PROGRAM_CODE, TEST_ORDERABLE_CODE,
            TEST_TOTAL_STOCK_ON_HAND, TEST_STOCK_CARDS);

    assertEquals(TEST_PROGRAM_CODE, summary.getProgram());
    assertEquals(TEST_ORDERABLE_CODE, summary.getOrderable());
    assertEquals(TEST_TOTAL_STOCK_ON_HAND, summary.getTotalStockOnHand());
    assertEquals(TEST_STOCK_CARDS, summary.getStockCards());
  }

  @Test
  public void shouldSet() {
    final StockCardSummaryExternalDto summary = new StockCardSummaryExternalDto();

    summary.setProgram(TEST_PROGRAM_CODE);
    summary.setOrderable(TEST_ORDERABLE_CODE);
    summary.setTotalStockOnHand(TEST_TOTAL_STOCK_ON_HAND);
    summary.setStockCards(TEST_STOCK_CARDS);

    assertEquals(TEST_PROGRAM_CODE, summary.getProgram());
    assertEquals(TEST_ORDERABLE_CODE, summary.getOrderable());
    assertEquals(TEST_TOTAL_STOCK_ON_HAND, summary.getTotalStockOnHand());
    assertEquals(TEST_STOCK_CARDS, summary.getStockCards());
  }

}
