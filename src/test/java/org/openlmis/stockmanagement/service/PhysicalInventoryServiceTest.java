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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryServiceTest {
  @InjectMocks
  private PhysicalInventoryService physicalInventoryService;

  @Mock
  private PhysicalInventoriesRepository physicalInventoriesRepository;

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
    physicalInventoryService.submitPhysicalInventory(piDto, UUID.randomUUID());

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