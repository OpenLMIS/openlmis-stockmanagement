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

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import java.time.LocalDate;
import java.util.Collections;

public class PhysicalInventoryDtoTest {
  @Test
  public void should_convert_from_stock_event_dto() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDto();

    //when
    PhysicalInventoryDto piDto = PhysicalInventoryDto.fromEventDto(eventDto);

    //then
    assertThat(piDto.getProgramId(), is(eventDto.getProgramId()));
    assertThat(piDto.getFacilityId(), is(eventDto.getFacilityId()));
    assertThat(piDto.getSignature(), is(eventDto.getSignature()));
    assertThat(piDto.getDocumentNumber(), is(eventDto.getDocumentNumber()));
    assertThat(piDto.getOccurredDate(), is(eventDto.getLineItems().get(0).getOccurredDate()));
  }

  @Test
  public void should_convert_into_inventory_jpa_model_for_submit() throws Exception {
    //given
    PhysicalInventoryDto piDto = createInventoryDto();

    //when
    PhysicalInventory inventory = piDto.toPhysicalInventoryForSubmit();

    //then
    fieldsEqual(piDto, inventory);

    assertThat(inventory.getIsDraft(), is(false));
  }

  @Test
  public void should_convert_into_inventory_jpa_model_for_draft() throws Exception {
    //given
    PhysicalInventoryDto piDto = createInventoryDto();

    //when
    PhysicalInventory inventory = piDto.toPhysicalInventoryForDraft();

    //then
    fieldsEqual(piDto, inventory);

    assertThat(inventory.getIsDraft(), is(true));
  }

  @Test
  public void should_create_dto_from_jpa_model() throws Exception {
    //given
    PhysicalInventory inventory = createInventoryDto().toPhysicalInventoryForDraft();

    //when
    PhysicalInventoryDto dto = PhysicalInventoryDto.from(inventory);

    //then
    assertThat(dto.getProgramId(), is(inventory.getProgramId()));
    assertThat(dto.getFacilityId(), is(inventory.getFacilityId()));
    assertThat(dto.getOccurredDate(), is(inventory.getOccurredDate()));
    assertThat(dto.getDocumentNumber(), is(inventory.getDocumentNumber()));
    assertThat(dto.getSignature(), is(inventory.getSignature()));
    assertThat(dto.getIsStarter(), is(false));

    assertThat(dto.getLineItems().size(), is(1));
  }

  private PhysicalInventoryDto createInventoryDto() {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setOccurredDate(LocalDate.now());
    piDto.setFacilityId(randomUUID());
    piDto.setProgramId(randomUUID());
    piDto.setSignature("test");
    piDto.setDocumentNumber(null);

    PhysicalInventoryLineItemDto piLineItemDto1 = new PhysicalInventoryLineItemDto();
    piLineItemDto1.setOrderableId(randomUUID());
    piLineItemDto1.setQuantity(123);

    piDto.setLineItems(Collections.singletonList(piLineItemDto1));
    return piDto;
  }

  private void fieldsEqual(PhysicalInventoryDto piDto, PhysicalInventory inventory) {
    assertThat(inventory.getOccurredDate(), is(piDto.getOccurredDate()));
    assertThat(inventory.getProgramId(), is(piDto.getProgramId()));
    assertThat(inventory.getFacilityId(), is(piDto.getFacilityId()));
    assertThat(inventory.getSignature(), is(piDto.getSignature()));
    assertThat(inventory.getDocumentNumber(), is(piDto.getDocumentNumber()));

    assertThat(inventory.getLineItems().size(), is(1));
    PhysicalInventoryLineItemDto piLineItemDto = piDto.getLineItems().get(0);
    PhysicalInventoryLineItem piLineItem = inventory.getLineItems().get(0);
    assertThat(piLineItem.getQuantity(), is(piLineItemDto.getQuantity()));
    assertThat(piLineItem.getOrderableId(), is(piLineItemDto.getOrderableId()));
  }
}