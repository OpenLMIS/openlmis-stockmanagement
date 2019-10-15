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

package org.openlmis.stockmanagement.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.testutils.CanFulfillForMeEntryDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardSummaryV2DtoDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;
import org.openlmis.stockmanagement.web.stockcardsummariesv2.CanFulfillForMeEntryDto;
import org.openlmis.stockmanagement.web.stockcardsummariesv2.StockCardSummaryV2Dto;

public class StockCardSummaryV2DtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(StockCardSummaryV2Dto.class)
        .suppress(Warning.NONFINAL_FIELDS) // fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    StockCardSummaryV2Dto stockCard = new StockCardSummaryV2DtoDataBuilder().build();
    ToStringTestUtils.verify(StockCardSummaryV2Dto.class, stockCard);
  }

  @Test
  public void shouldGetStockOnHand() {
    StockCardSummaryV2Dto stockCard = new StockCardSummaryV2DtoDataBuilder()
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder()
            .withStockOnHand(null).build())
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder()
            .withStockOnHand(12).build())
        .build();
    assertEquals(new Integer(12), stockCard.getStockOnHand());
  }

  @Test
  public void shouldGetNullStockOnHandIfCanFulfillForMeIsNullOrEmpty() {
    StockCardSummaryV2Dto stockCard = new StockCardSummaryV2DtoDataBuilder()
        .withCanFulfillForMe((Set<CanFulfillForMeEntryDto>) null)
        .build();
    assertEquals(null, stockCard.getStockOnHand());

    stockCard = new StockCardSummaryV2DtoDataBuilder().build();
    assertEquals(null, stockCard.getStockOnHand());
  }

  @Test
  public void shouldGetNullStockOnHandIfCanFulfillDoesNotHaveStockOnHand() {
    StockCardSummaryV2Dto stockCard = new StockCardSummaryV2DtoDataBuilder()
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder()
            .withStockOnHand(null)
            .build())
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder()
            .withStockOnHand(null)
            .build())
        .build();
    assertEquals(null, stockCard.getStockOnHand());
  }

  @Test
  public void shouldCompareStockCards() {
    StockCardSummaryV2Dto stockCard1 = new StockCardSummaryV2DtoDataBuilder()
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder().build())
        .build();
    StockCardSummaryV2Dto stockCard2 = new StockCardSummaryV2DtoDataBuilder()
        .build();
    assertEquals(-1, stockCard1.compareTo(stockCard2));

    stockCard2 = new StockCardSummaryV2DtoDataBuilder()
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder().build())
        .build();
    assertEquals(0, stockCard1.compareTo(stockCard2));

    stockCard1 = new StockCardSummaryV2DtoDataBuilder().build();
    stockCard2 = new StockCardSummaryV2DtoDataBuilder().build();
    assertEquals(0, stockCard1.compareTo(stockCard2));

    stockCard2 = new StockCardSummaryV2DtoDataBuilder()
        .withCanFulfillForMe(new CanFulfillForMeEntryDtoDataBuilder().build())
        .build();
    assertEquals(1, stockCard1.compareTo(stockCard2));
  }

  @Test
  public void shouldEnsureComparableContracts() {
    StockCardSummaryV2Dto stockCard1 = new StockCardSummaryV2DtoDataBuilder()
            .build();
    StockCardSummaryV2Dto stockCard2 = new StockCardSummaryV2DtoDataBuilder()
            .build();

    assertEquals(Math.signum(stockCard1.compareTo(stockCard2)),
            -Math.signum(stockCard2.compareTo(stockCard1)), 0);

    stockCard1 = new StockCardSummaryV2DtoDataBuilder()
            .withCanFulfillForMe(new HashSet<>(Arrays.asList(
                    new CanFulfillForMeEntryDto(null, null, null, 15, null, null),
                    new CanFulfillForMeEntryDto())))
            .build();
    stockCard2 = new StockCardSummaryV2DtoDataBuilder()
            .withCanFulfillForMe(new CanFulfillForMeEntryDto())
            .build();

    assertEquals(Math.signum(stockCard1.compareTo(stockCard2)),
            -Math.signum(stockCard2.compareTo(stockCard1)), 0);

    StockCardSummaryV2Dto stockCard3 = new StockCardSummaryV2DtoDataBuilder()
            .build();

    assertTrue(stockCard3.compareTo(stockCard2) > 0);
    assertTrue(stockCard2.compareTo(stockCard1) > 0);
    assertTrue(stockCard3.compareTo(stockCard1) > 0);

    stockCard1 = new StockCardSummaryV2DtoDataBuilder()
            .withCanFulfillForMe(new CanFulfillForMeEntryDto())
            .build();
    stockCard2 = new StockCardSummaryV2DtoDataBuilder()
            .withCanFulfillForMe(new CanFulfillForMeEntryDto())
            .build();

    assertEquals(stockCard1.compareTo(stockCard2), 0);
    assertEquals(Math.signum(stockCard1.compareTo(stockCard3)),
            Math.signum(stockCard2.compareTo(stockCard3)), 0);

  }
}
