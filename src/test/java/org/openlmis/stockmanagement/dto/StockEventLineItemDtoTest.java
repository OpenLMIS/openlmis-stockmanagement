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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;

public class StockEventLineItemDtoTest {
  @Test
  public void should_convert_from_dto_to_jpa_model() throws Exception {
    //given
    StockEventLineItemDto lineItemDto = StockEventDtoDataBuilder.createStockEventLineItem();

    //when
    StockEventLineItem lineItem = lineItemDto.toEventLineItem();

    //then
    assertThat(lineItem.getReasonFreeText(), is(lineItemDto.getReasonFreeText()));
    assertThat(lineItem.getReasonId(), is(lineItemDto.getReasonId()));
    assertThat(lineItem.getQuantity(), is(lineItemDto.getQuantity()));
    assertThat(lineItem.getOrderableId(), is(lineItemDto.getOrderableId()));
    assertThat(lineItem.getOccurredDate(), is(lineItemDto.getOccurredDate()));
    assertThat(lineItem.getSourceId(), is(lineItemDto.getSourceId()));
    assertThat(lineItem.getDestinationId(), is(lineItemDto.getDestinationId()));
    assertThat(lineItem.getSourceFreeText(), is(lineItemDto.getSourceFreeText()));
    assertThat(lineItem.getDestinationFreeText(), is(lineItemDto.getDestinationFreeText()));
  }

}