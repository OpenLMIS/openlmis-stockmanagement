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

package org.openlmis.stockmanagement.web;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.CanFulfillForMeEntryDto;
import org.openlmis.stockmanagement.dto.StockCardSummaryV2Dto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableFulfillDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.web.stockcardsummariesv2.StockCardSummariesV2DtoBuilder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesV2DtoBuilderTest {

  @InjectMocks
  private StockCardSummariesV2DtoBuilder builder;

  UUID programId = randomUUID();
  UUID facilityId = randomUUID();
  OrderableDto orderable1;
  OrderableDto orderable2;
  OrderableDto orderable3;
  StockCard stockCard;
  StockCard stockCard1;
  StockCard stockCard2;
  Map<UUID, OrderableFulfillDto> fulfillMap;

  @Before
  public void before() {
    orderable1 = new OrderableDtoDataBuilder().build();
    orderable2 = new OrderableDtoDataBuilder().build();
    orderable3 = new OrderableDtoDataBuilder().build();

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();
    stockCard = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(12,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(16),
            orderable1.getId());
    stockCard1 = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(26,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(30),
            orderable3.getId());
    stockCard2 = new StockCardDataBuilder(event)
        .withLot(UUID.randomUUID())
        .buildWithStockOnHandAndLineItemAndOrderableId(22,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(10),
            orderable3.getId());

    fulfillMap = new HashMap<>();
    fulfillMap.put(orderable1.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2.getId(), orderable3.getId())).build());
    fulfillMap.put(orderable2.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable1.getId(), orderable3.getId())).build());
  }

  @Test
  public void shouldBuildStockCardSummaries() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable1, orderable2, orderable3),
        stockCards,fulfillMap, asOfDate);

    assertEquals(3, result.size());

    for (StockCardSummaryV2Dto summary : result) {
      if (summary.getOrderable().getId().equals(orderable1.getId())) {
        assertEquals(new Integer(30), summary.getStockOnHand());
        for (CanFulfillForMeEntryDto entry : summary.getCanFulfillForMe()) {
          if (entry.getOrderable().getId().equals(orderable2.getId())) {
            assertEquals(null, entry.getStockCard());
          } else {
            assertEquals(new Integer(30), entry.getStockOnHand());
          }
        }
      } else if (summary.getOrderable().getId().equals(orderable2.getId())) {
        assertEquals(new Integer(46), summary.getStockOnHand());
        for (CanFulfillForMeEntryDto entry : summary.getCanFulfillForMe()) {
          if (entry.getOrderable().getId().equals(orderable1.getId())) {
            assertEquals(new Integer(16), entry.getStockOnHand());
          } else {
            assertEquals(new Integer(30), entry.getStockOnHand());
          }
        }
      } else {
        assertEquals(null, summary.getStockOnHand());
        assertEquals(null, summary.getCanFulfillForMe());
      }
    }
  }

  @Test
  public void shouldBuildStockCardSummariesWithMultipleStockCardsForOrderable() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1, stockCard2);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable1, orderable2, orderable3),
        stockCards,fulfillMap, asOfDate);

    assertEquals(3, result.size());

    for (StockCardSummaryV2Dto summary : result) {
      if (summary.getOrderable().getId().equals(orderable1.getId())) {
        assertEquals(new Integer(40), summary.getStockOnHand());
      } else if (summary.getOrderable().getId().equals(orderable2.getId())) {
        assertEquals(new Integer(56), summary.getStockOnHand());
      } else {
        assertEquals(null, summary.getStockOnHand());
        assertEquals(null, summary.getCanFulfillForMe());
      }
    }
  }
}
