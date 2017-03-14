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

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryServiceTest {
  @InjectMocks
  private PhysicalInventoryService physicalInventoryService;

  @Mock
  private StockEventProcessor stockEventProcessor;

  @Mock
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_line_items_not_exist() throws Exception {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    piDto.setLineItems(null);

    physicalInventoryService.createPhysicalInventory(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_line_items_is_empty() throws Exception {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    piDto.setLineItems(new ArrayList<>());

    physicalInventoryService.createPhysicalInventory(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_orderable_not_exist() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piDto.setLineItems(singletonList(piLineItemDto));

    physicalInventoryService.createPhysicalInventory(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_orderables_are_duplicate() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);

    UUID orderableId1 = UUID.fromString("944ba21e-5ef8-4a83-95bf-09d82a9a5271");
    OrderableDto orderable1 = OrderableDto.builder().id(orderableId1).build();
    PhysicalInventoryLineItemDto piLineItemDto1 = new PhysicalInventoryLineItemDto();
    piLineItemDto1.setOrderable(orderable1);

    UUID orderableId2 = UUID.fromString("944ba21e-5ef8-4a83-95bf-09d82a9a5271");
    OrderableDto orderable2 = OrderableDto.builder().id(orderableId2).build();
    PhysicalInventoryLineItemDto piLineItemDto2 = new PhysicalInventoryLineItemDto();
    piLineItemDto2.setOrderable(orderable2);

    piDto.setLineItems(Arrays.asList(piLineItemDto1, piLineItemDto2));

    //when
    physicalInventoryService.createPhysicalInventory(piDto);
  }

  @Test
  public void should_associate_inventory_with_created_events() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piLineItemDto.setOrderable(OrderableDto.builder().id(randomUUID()).build());
    piDto.setLineItems(singletonList(piLineItemDto));

    UUID eventId = UUID.randomUUID();
    UUID inventoryId = UUID.randomUUID();

    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setId(inventoryId);

    when(stockEventProcessor.process(any(StockEventDto.class))).thenReturn(eventId);
    when(physicalInventoriesRepository.save(any(PhysicalInventory.class))).thenReturn(inventory);

    //when
    physicalInventoryService.createPhysicalInventory(piDto);

    //then
    ArgumentCaptor<PhysicalInventory> inventoryArgumentCaptor = forClass(PhysicalInventory.class);

    verify(physicalInventoriesRepository, times(1)).save(inventoryArgumentCaptor.capture());
    StockEvent event = inventoryArgumentCaptor.getValue().getStockEvents().get(0);

    assertThat(event.getId(), is(eventId));
  }
}