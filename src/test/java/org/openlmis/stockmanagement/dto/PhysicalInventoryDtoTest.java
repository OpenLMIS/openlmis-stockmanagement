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

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PhysicalInventoryDtoTest {
  @Test
  public void should_convert_into_stock_event_dtos() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setOccurredDate(ZonedDateTime.now());
    piDto.setFacilityId(UUID.randomUUID());
    piDto.setProgramId(UUID.randomUUID());
    piDto.setSignature("test");
    piDto.setDocumentNumber(null);

    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piLineItemDto.setOrderableDto(OrderableDto.builder().id(UUID.randomUUID()).build());
    piLineItemDto.setQuantity(123);

    piDto.setLineItems(Arrays.asList(piLineItemDto));

    //when
    List<StockEventDto> eventDtos = piDto.toEventDtos();

    //then
    assertThat(eventDtos.size(), is(1));
    StockEventDto eventDto = eventDtos.get(0);
    assertThat(eventDto.getProgramId(), is(piDto.getProgramId()));
    assertThat(eventDto.getFacilityId(), is(piDto.getFacilityId()));
    assertThat(eventDto.getOrderableId(), is(piLineItemDto.getOrderableDto().getId()));

    assertThat(eventDto.getSignature(), is(piDto.getSignature()));
    assertThat(eventDto.getDocumentNumber(), is(piDto.getDocumentNumber()));

    assertThat(eventDto.getOccurredDate(), is(piDto.getOccurredDate()));
    assertThat(eventDto.getQuantity(), is(piLineItemDto.getQuantity()));
  }
}