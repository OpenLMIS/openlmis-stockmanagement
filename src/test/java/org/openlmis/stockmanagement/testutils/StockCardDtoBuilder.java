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

package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.domain.adjustment.ReasonCategory;
import org.openlmis.stockmanagement.domain.adjustment.ReasonType;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class StockCardDtoBuilder {
  /**
   * Create stock card dto.
   *
   * @return stock card dto
   */
  public static StockCardDto createStockCardDto() {
    StockCardLineItemReason reason = StockCardLineItemReason
        .builder()
        .name("Transfer In")
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.CREDIT).build();


    StockCardLineItem lineItem = StockCardLineItem
        .builder()
        .stockOnHand(1)
        .quantity(1)
        .occurredDate(ZonedDateTime.of(2017, 2, 13, 4, 2, 18, 781000000, ZoneId.of("UTC")))
        .reason(reason).build();

    StockCardLineItemDto lineItemDto = StockCardLineItemDto
        .builder()
        .lineItem(lineItem)
        .source(FacilityDto.builder().name("HF1").build())
        .build();

    return StockCardDto.builder()
        .stockOnHand(1)
        .facility(FacilityDto.builder().name("HC01").build())
        .program(ProgramDto.builder().name("HIV").build())
        .orderable(OrderableDto.builder().productCode("ABC01").build())
        .lineItems(Arrays.asList(lineItemDto))
        .build();
  }
}
