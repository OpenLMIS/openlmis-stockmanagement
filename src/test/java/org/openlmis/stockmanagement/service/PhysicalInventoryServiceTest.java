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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.validators.PhysicalInventoryValidator;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryServiceTest {
  @Captor
  private ArgumentCaptor<PhysicalInventory> inventoryArgumentCaptor;

  @Mock
  private PhysicalInventoryValidator validator;

  @Mock
  private PhysicalInventoriesRepository physicalInventoryRepository;

  @Mock
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private PhysicalInventoryService physicalInventoryService;

  @Test
  public void shouldReturnDraftIfSavedDraftIsFound() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    UUID orderableId = randomUUID();

    PhysicalInventory inventory = createInventoryDraft(orderableId, programId, facilityId);

    when(physicalInventoryRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(inventory);

    //when
    PhysicalInventoryDto foundDraft = physicalInventoryService.findDraft(programId, facilityId);

    //then
    PhysicalInventoryLineItemDto lineItemDto = foundDraft.getLineItems().get(0);
    assertThat(lineItemDto.getOrderable().getId(), is(orderableId));
  }

  @Test
  public void shouldSaveDraftWhenPassValidations() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);
    UUID physicalInventoryId = randomUUID();

    //when
    physicalInventoryService.saveDraft(piDto, physicalInventoryId);

    //then
    verify(physicalInventoryRepository, times(1)).save(inventoryArgumentCaptor.capture());
    PhysicalInventory captured = inventoryArgumentCaptor.getValue();
    verifyLineItems(piDto.getLineItems(), captured.getLineItems());
    assertEquals(programId, captured.getProgramId());
    assertEquals(facilityId, captured.getFacilityId());
    assertEquals(true, captured.getIsDraft());

    verify(homeFacilityPermissionService, times(1)).checkProgramSupported(programId);
    verify(permissionService, times(1)).canEditPhysicalInventory(programId, facilityId);
    verify(validator, times(1)).validateDraft(piDto, physicalInventoryId);
  }

  @Test
  public void shouldCreateNewDraft() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);

    //when
    physicalInventoryService.createNewDraft(piDto);

    //then
    verify(physicalInventoryRepository, times(1)).save(inventoryArgumentCaptor.capture());
    PhysicalInventory captured = inventoryArgumentCaptor.getValue();
    assertEquals(programId, captured.getProgramId());
    assertEquals(facilityId, captured.getFacilityId());
    assertEquals(true, captured.getIsDraft());
    assertEquals(null, captured.getLineItems());

    verify(homeFacilityPermissionService, times(1)).checkProgramSupported(programId);
    verify(permissionService, times(1)).canEditPhysicalInventory(programId, facilityId);
    verify(validator, times(1)).validateEmptyDraft(piDto);
  }

  @Test
  public void shouldDeleteDraftWhenPassValidations() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventory physicalInventory =
        createInventoryDraft(UUID.randomUUID(), programId, facilityId);
    physicalInventory.setId(UUID.randomUUID());
    when(physicalInventoryRepository.findOne(physicalInventory.getId()))
        .thenReturn(physicalInventory);

    //when
    physicalInventoryService.deletePhysicalInventory(physicalInventory.getId());

    //then
    verify(physicalInventoryRepository, times(1)).delete(physicalInventory);

    verify(homeFacilityPermissionService, times(1)).checkProgramSupported(programId);
    verify(permissionService, times(1)).canEditPhysicalInventory(programId, facilityId);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowExceptionWhenDeleteIfInventoryNotFound() throws Exception {
    UUID physicalInventoryId = UUID.randomUUID();
    when(physicalInventoryRepository.findOne(physicalInventoryId)).thenReturn(null);

    physicalInventoryService.deletePhysicalInventory(physicalInventoryId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenDeleteIfInventoryIsNotDraft() throws Exception {
    UUID physicalInventoryId = UUID.randomUUID();
    PhysicalInventory physicalInventory = mock(PhysicalInventory.class);
    when(physicalInventory.getIsDraft()).thenReturn(false);
    when(physicalInventoryRepository.findOne(physicalInventoryId))
        .thenReturn(physicalInventory);

    physicalInventoryService.deletePhysicalInventory(physicalInventoryId);
  }

  private PhysicalInventoryDto createInventoryDto(UUID programId, UUID facilityId) {
    PhysicalInventoryLineItemDto piLineItemDto = PhysicalInventoryLineItemDto
        .builder()
        .orderable(OrderableDto.builder().id(randomUUID()).build())
        .quantity(null)//quantity is allowed to be null
        .build();
    return PhysicalInventoryDto
        .builder()
        .programId(programId)
        .facilityId(facilityId)
        .lineItems(singletonList(piLineItemDto))
        .build();
  }

  private void verifyLineItems(List<PhysicalInventoryLineItemDto> expected,
                               List<PhysicalInventoryLineItem> actual) {
    PhysicalInventoryLineItemDto lineItemDto = expected.get(0);
    PhysicalInventoryLineItem lineItem = actual.get(0);
    assertThat(lineItem.getQuantity(), is(lineItemDto.getQuantity()));
    assertThat(lineItem.getOrderableId(), is(lineItemDto.getOrderable().getId()));
  }

  private PhysicalInventory createInventoryDraft(
      UUID orderableId, UUID programId, UUID facilityId) {
    PhysicalInventory inventory = createInventoryDto(programId, facilityId)
        .toPhysicalInventoryForDraft();
    inventory.getLineItems().get(0).setOrderableId(orderableId);
    return inventory;
  }
}