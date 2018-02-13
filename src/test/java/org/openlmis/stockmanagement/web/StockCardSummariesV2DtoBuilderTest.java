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

  @Test
  public void shouldBuildStockCardSummaries() throws Exception {
    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    OrderableDto orderable1 = new OrderableDtoDataBuilder().build();
    OrderableDto orderable2 = new OrderableDtoDataBuilder().build();
    OrderableDto orderable3 = new OrderableDtoDataBuilder().build();

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();
    StockCard stockCard = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(12,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(16),
            orderable1.getId());
    StockCard stockCard1 = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(26,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(30),
            orderable3.getId());
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderable1.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2.getId(), orderable3.getId())).build());
    fulfillMap.put(orderable2.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable1.getId(), orderable3.getId())).build());

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
}
