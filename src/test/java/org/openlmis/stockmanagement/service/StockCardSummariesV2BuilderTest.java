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

package org.openlmis.stockmanagement.service;

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
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.testutils.OrderableFulfillDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesV2BuilderTest {

  @InjectMocks
  private StockCardSummariesV2Builder builder;

  @Test
  public void shouldBuildStockCardSummaries() throws Exception {
    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    UUID orderable1Id = randomUUID();
    UUID orderable2Id = randomUUID();
    UUID orderable3Id = randomUUID();

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();
    StockCard stockCard = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(12,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(16),
            orderable1Id);
    StockCard stockCard1 = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(26,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(30),
            orderable3Id);
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderable1Id, new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2Id, orderable3Id)).build());
    fulfillMap.put(orderable2Id, new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable1Id, orderable3Id)).build());

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(stockCards,fulfillMap, asOfDate);

    assertEquals(2, result.size());

    for (StockCardSummaryV2Dto summary : result) {
      if (summary.getOrderable().getId().equals(orderable1Id)) {
        assertEquals(new Integer(30), summary.getStockOnHand());
        for (CanFulfillForMeEntryDto entry : summary.getCanFulfillForMe()) {
          if (entry.getOrderable().getId().equals(orderable2Id)) {
            assertEquals(null, entry.getStockCard());
          } else {
            assertEquals(new Integer(30), entry.getStockOnHand());
          }
        }
      } else {
        assertEquals(new Integer(46), summary.getStockOnHand());
        for (CanFulfillForMeEntryDto entry : summary.getCanFulfillForMe()) {
          if (entry.getOrderable().getId().equals(orderable1Id)) {
            assertEquals(new Integer(16), entry.getStockOnHand());
          } else {
            assertEquals(new Integer(30), entry.getStockOnHand());
          }
        }
      }
    }
  }
}
