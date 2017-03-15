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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.ProgramOrderableDto;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryServiceDraftTest {
  @InjectMocks
  private PhysicalInventoryService physicalInventoryService;

  @Mock
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Mock
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Test
  public void should_generate_empty_draft_if_no_saved_draft_is_found() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(null);

    UUID orderableId = UUID.randomUUID();
    ProgramOrderableDto programOrderable = new ProgramOrderableDto();
    programOrderable.setOrderableId(orderableId);
    ApprovedProductDto approvedProductDto = new ApprovedProductDto();
    approvedProductDto.setProgramOrderable(programOrderable);

    when(approvedProductReferenceDataService.getAllApprovedProducts(programId, facilityId))
        .thenReturn(singletonList(approvedProductDto));

    OrderableDto orderableDto = new OrderableDto();
    when(orderableReferenceDataService.findOne(orderableId)).thenReturn(orderableDto);

    //when
    PhysicalInventoryDto inventory = physicalInventoryService.findDraft(programId, facilityId);

    //then
    assertThat(inventory.getProgramId(), is(programId));
    assertThat(inventory.getFacilityId(), is(facilityId));
    assertThat(inventory.getLineItems().size(), is(1));

    PhysicalInventoryLineItemDto inventoryLineItemDto = inventory.getLineItems().get(0);
    assertThat(inventoryLineItemDto.getOrderable(), is(orderableDto));
    assertThat(inventoryLineItemDto.getQuantity(), nullValue());
  }

  @Test
  public void should_save_draft_if_no_saved_draft_is_found() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(null);

    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);

    //when
    physicalInventoryService.saveDraft(piDto);

    //then
    ArgumentCaptor<PhysicalInventory> inventoryArgumentCaptor = forClass(PhysicalInventory.class);
    verify(physicalInventoriesRepository, times(1)).save(inventoryArgumentCaptor.capture());

    PhysicalInventoryLineItemDto lineItemDto = piDto.getLineItems().get(0);
    PhysicalInventoryLineItem lineItem = inventoryArgumentCaptor.getValue().getLineItems().get(0);
    assertThat(lineItem.getQuantity(), is(lineItemDto.getQuantity()));
    assertThat(lineItem.getOrderableId(), is(lineItemDto.getOrderable().getId()));
  }

  @Test
  public void should_replace_saved_draft() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    PhysicalInventory savedDraft = new PhysicalInventory();
    when(physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(savedDraft);

    PhysicalInventoryDto newDraft = createInventoryDto(programId, facilityId);

    //when
    physicalInventoryService.saveDraft(newDraft);

    //then
    verify(physicalInventoriesRepository, times(1)).delete(savedDraft);

    ArgumentCaptor<PhysicalInventory> inventoryArgumentCaptor = forClass(PhysicalInventory.class);
    verify(physicalInventoriesRepository, times(1)).save(inventoryArgumentCaptor.capture());

    PhysicalInventoryLineItemDto lineItemDto = newDraft.getLineItems().get(0);
    PhysicalInventoryLineItem lineItem = inventoryArgumentCaptor.getValue().getLineItems().get(0);
    assertThat(lineItem.getQuantity(), is(lineItemDto.getQuantity()));
    assertThat(lineItem.getOrderableId(), is(lineItemDto.getOrderable().getId()));
  }

  @Test
  public void should_return_draft_if_saved_draft_is_found() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    UUID orderableId = randomUUID();

    PhysicalInventory inventory = createInventory(orderableId);

    when(physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(inventory);

    OrderableDto orderableDto = new OrderableDto();
    when(orderableReferenceDataService.findOne(orderableId))
        .thenReturn(orderableDto);

    //when
    PhysicalInventoryDto foundDraft = physicalInventoryService.findDraft(programId, facilityId);

    //then
    PhysicalInventoryLineItemDto lineItemDto = foundDraft.getLineItems().get(0);
    assertThat(lineItemDto.getOrderable(), is(orderableDto));
  }

  private PhysicalInventory createInventory(UUID orderableId) {
    PhysicalInventory inventory = createInventoryDto(randomUUID(), randomUUID())
        .toPhysicalInventoryForDraft();
    inventory.getLineItems().get(0).setOrderableId(orderableId);
    return inventory;
  }

  private PhysicalInventoryDto createInventoryDto(UUID programId, UUID facilityId) {
    PhysicalInventoryLineItemDto piLineItemDto = PhysicalInventoryLineItemDto
        .builder()
        .orderable(OrderableDto.builder().id(randomUUID()).build())
        .quantity(233)
        .build();
    return PhysicalInventoryDto
        .builder()
        .programId(programId).facilityId(facilityId)
        .lineItems(singletonList(piLineItemDto))
        .build();
  }
}