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
import static org.mockito.Matchers.anyObject;
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
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;

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

  @Mock
  private StockCardRepository stockCardRepository;

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_line_items_not_exist() throws Exception {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setLineItems(null);

    physicalInventoryService.submitPhysicalInventory(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_line_items_is_empty() throws Exception {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setLineItems(new ArrayList<>());

    physicalInventoryService.submitPhysicalInventory(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_orderable_not_exist() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piDto.setLineItems(singletonList(piLineItemDto));

    physicalInventoryService.submitPhysicalInventory(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_active_stock_card_not_included()
      throws Exception {
    //given
    UUID orderableId1 = randomUUID();
    UUID orderableId2 = randomUUID();

    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    when(stockCardRepository.getStockCardOrderableIdsBy(programId, facilityId))
        .thenReturn(Arrays.asList(orderableId1, orderableId2));

    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setProgramId(programId);
    piDto.setFacilityId(facilityId);
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piLineItemDto.setOrderable(OrderableDto.builder().id(orderableId1).build());
    piDto.setLineItems(singletonList(piLineItemDto));

    //when
    physicalInventoryService.submitPhysicalInventory(piDto);
  }

  @Test
  public void should_associate_inventory_with_created_event() throws Exception {
    //given
    PhysicalInventoryDto piDto = createInventoryDto();

    UUID eventId = UUID.randomUUID();

    when(stockEventProcessor.process(anyObject())).thenReturn(eventId);
    when(physicalInventoriesRepository.save(any(PhysicalInventory.class)))
        .thenReturn(new PhysicalInventory());

    //when
    physicalInventoryService.submitPhysicalInventory(piDto);

    //then
    ArgumentCaptor<PhysicalInventory> inventoryArgumentCaptor = forClass(PhysicalInventory.class);

    verify(physicalInventoriesRepository, times(1)).save(inventoryArgumentCaptor.capture());
    StockEvent event = inventoryArgumentCaptor.getValue().getStockEvent();

    assertThat(event.getId(), is(eventId));
  }

  @Test
  public void should_delete_draft_when_submit_inventory() throws Exception {
    //given
    PhysicalInventoryDto piDto = createInventoryDto();


    when(physicalInventoriesRepository.save(any(PhysicalInventory.class)))
        .thenReturn(new PhysicalInventory());

    PhysicalInventory savedDraft = new PhysicalInventory();
    when(physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(piDto.getProgramId(), piDto.getFacilityId(), true))
        .thenReturn(savedDraft);

    //when
    physicalInventoryService.submitPhysicalInventory(piDto);

    //then
    verify(physicalInventoriesRepository, times(1)).delete(savedDraft);
  }

  private PhysicalInventoryDto createInventoryDto() {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setProgramId(UUID.randomUUID());
    piDto.setFacilityId(UUID.randomUUID());
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piLineItemDto.setOrderable(OrderableDto.builder().id(randomUUID()).build());
    piDto.setLineItems(singletonList(piLineItemDto));
    return piDto;
  }

}