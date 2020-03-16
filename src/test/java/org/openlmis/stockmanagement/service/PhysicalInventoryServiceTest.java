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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.testutils.SaveAnswer;
import org.openlmis.stockmanagement.validators.PhysicalInventoryValidator;

@SuppressWarnings("PMD.TooManyMethods")
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

  @Mock
  private CalculatedStockOnHandService calculatedStockOnHandService;

  @InjectMocks
  private PhysicalInventoryService physicalInventoryService;

  private StockCard stockCard = mock(StockCard.class);
  private UUID programId = randomUUID();
  private UUID facilityId = randomUUID();
  private UUID orderableId = randomUUID();
  private PhysicalInventoryLineItemDto lineItemDto;

  @Test
  public void shouldSubmitPhysicalInventory() {
    PhysicalInventoryDto physicalInventoryDto = newInventoryForSubmit();
    int previousSoH = new Random().nextInt();
    when(calculatedStockOnHandService.getStockCardsWithStockOnHand(
        physicalInventoryDto.getProgramId(),
        physicalInventoryDto.getFacilityId()))
        .thenReturn(singletonList(stockCard));
    when(stockCard.getOrderableId()).thenReturn(lineItemDto.getOrderableId());
    when(stockCard.getLotId()).thenReturn(lineItemDto.getLotId());
    when(stockCard.getStockOnHand()).thenReturn(previousSoH);

    physicalInventoryService.submitPhysicalInventory(physicalInventoryDto, UUID.randomUUID());

    verify(physicalInventoryRepository, times(1)).save(inventoryArgumentCaptor.capture());

    verifyPhysicalInventorySavedWithSohAndAsDraft(previousSoH);
  }

  @Test
  public void shouldLeavePreviousSohAsNullWhenSubmitPhysicalInventoryIfNoStockCardFound() {
    PhysicalInventoryDto physicalInventoryDto = newInventoryForSubmit();
    when(calculatedStockOnHandService.getStockCardsWithStockOnHand(
        physicalInventoryDto.getProgramId(),
        physicalInventoryDto.getFacilityId()))
        .thenReturn(emptyList());

    physicalInventoryService.submitPhysicalInventory(physicalInventoryDto, UUID.randomUUID());

    verify(physicalInventoryRepository, times(1)).save(inventoryArgumentCaptor.capture());
    verify(stockCard, never()).shallowCopy();

    verifyPhysicalInventorySavedWithoutSohAndAsDraft();
  }

  @Test
  public void shouldReturnDraftIfSavedDraftIsFound() {
    PhysicalInventory inventory = createInventoryDraft(orderableId, programId, facilityId);

    shouldSearchBasedOnIsDraft(inventory,true);
  }

  @Test
  public void shouldReturnSubmittedInventoryIfIsFound() {
    PhysicalInventory submittedInventory = createInventoryDraft(orderableId, programId, facilityId);

    shouldSearchBasedOnIsDraft(submittedInventory, false);
  }

  @Test
  public void shouldReturnAllSavedInventories() {
    PhysicalInventory inventory = createInventoryDraft(orderableId, programId, facilityId);

    when(physicalInventoryRepository
        .findByProgramIdAndFacilityId(programId, facilityId))
        .thenReturn(Collections.singletonList(inventory));

    List<PhysicalInventoryDto> foundDraft =
        physicalInventoryService.findPhysicalInventory(programId, facilityId, null);

    assertEquals(1, foundDraft.size());
    assertEquals(programId, foundDraft.get(0).getProgramId());
    assertEquals(facilityId, foundDraft.get(0).getFacilityId());

    PhysicalInventoryLineItemDto lineItemDto = foundDraft.get(0).getLineItems().get(0);
    assertEquals(orderableId, lineItemDto.getOrderableId());
    assertNull(lineItemDto.getQuantity());
  }

  @Test
  public void shouldCreateNewDraft() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);
    when(physicalInventoryRepository.save(any(PhysicalInventory.class)))
        .thenAnswer(new SaveAnswer<PhysicalInventory>());

    PhysicalInventoryDto newDraft = physicalInventoryService.createNewDraft(piDto);

    assertNotNull(newDraft.getId());
    verify(physicalInventoryRepository, times(1)).save(inventoryArgumentCaptor.capture());
    PhysicalInventory captured = inventoryArgumentCaptor.getValue();
    assertEquals(programId, captured.getProgramId());
    assertEquals(facilityId, captured.getFacilityId());
    assertEquals(true, captured.getIsDraft());
    assertNull(captured.getLineItems());

    verify(homeFacilityPermissionService, times(1)).checkProgramSupported(programId);
    verify(permissionService, times(1)).canEditPhysicalInventory(programId, facilityId);
    verify(validator, times(1)).validateEmptyDraft(piDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenCreateNewDraftIfExistsAlready() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);

    when(physicalInventoryRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(Collections.singletonList(mock(PhysicalInventory.class)));

    physicalInventoryService.createNewDraft(piDto);
  }

  @Test
  public void shouldSaveDraftWhenPassValidations() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);
    UUID physicalInventoryId = randomUUID();

    physicalInventoryService.saveDraft(piDto, physicalInventoryId);

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

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenSaveDraftIfExistsAlready() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);

    PhysicalInventory mock = mock(PhysicalInventory.class);
    when(mock.getId()).thenReturn(UUID.randomUUID());
    when(physicalInventoryRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(Collections.singletonList(mock));

    physicalInventoryService.saveDraft(piDto, piDto.getId());
  }

  @Test
  public void shouldNotThrowExceptionWhenSaveDraftIfExistingDraftHasSameId() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto piDto = createInventoryDto(programId, facilityId);

    PhysicalInventory mock = mock(PhysicalInventory.class);
    when(mock.getId()).thenReturn(piDto.getId());
    when(physicalInventoryRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true))
        .thenReturn(Collections.singletonList(mock));

    physicalInventoryService.saveDraft(piDto, piDto.getId());
  }

  @Test
  public void shouldDeleteDraftWhenPassValidations() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventory physicalInventory =
        createInventoryDraft(UUID.randomUUID(), programId, facilityId);
    physicalInventory.setId(UUID.randomUUID());
    when(physicalInventoryRepository.findById(physicalInventory.getId()))
        .thenReturn(Optional.of(physicalInventory));

    physicalInventoryService.deletePhysicalInventory(physicalInventory.getId());

    verify(physicalInventoryRepository, times(1)).delete(physicalInventory);

    verify(homeFacilityPermissionService, times(1)).checkProgramSupported(programId);
    verify(permissionService, times(1)).canEditPhysicalInventory(programId, facilityId);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowExceptionWhenDeleteIfInventoryNotFound() {
    UUID physicalInventoryId = UUID.randomUUID();
    when(physicalInventoryRepository.findById(physicalInventoryId)).thenReturn(Optional.empty());

    physicalInventoryService.deletePhysicalInventory(physicalInventoryId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenDeleteIfInventoryIsNotDraft() {
    UUID physicalInventoryId = UUID.randomUUID();
    PhysicalInventory physicalInventory = mock(PhysicalInventory.class);
    when(physicalInventory.getIsDraft()).thenReturn(false);
    when(physicalInventoryRepository.findById(physicalInventoryId))
        .thenReturn(Optional.of(physicalInventory));

    physicalInventoryService.deletePhysicalInventory(physicalInventoryId);
  }

  private PhysicalInventoryDto newInventoryForSubmit() {
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setId(randomUUID());
    inventory.setProgramId(randomUUID());
    inventory.setFacilityId(randomUUID());
    inventory.setIsDraft(true);
    lineItemDto = generateLineItem();
    inventory.setLineItems(Collections.singletonList(lineItemDto));
    return inventory;
  }

  private PhysicalInventoryLineItemDto generateLineItem() {
    return PhysicalInventoryLineItemDto
        .builder()
        .orderableId(UUID.randomUUID())
        .lotId(UUID.randomUUID())
        .quantity(5)
        .stockAdjustments(new ArrayList<>())
        .build();
  }

  private void verifyPhysicalInventorySavedWithSohAndAsDraft(int previousSoH) {
    PhysicalInventory captured = inventoryArgumentCaptor.getValue();
    Integer previousStockOnHand =
        captured.getLineItems().get(0).getPreviousStockOnHandWhenSubmitted();
    assertNotNull(previousStockOnHand);
    assertEquals(previousSoH, previousStockOnHand.intValue());
    assertFalse(captured.getIsDraft());
  }

  private void verifyPhysicalInventorySavedWithoutSohAndAsDraft() {
    PhysicalInventory captured = inventoryArgumentCaptor.getValue();
    Integer previousStockOnHand =
        captured.getLineItems().get(0).getPreviousStockOnHandWhenSubmitted();
    assertNull(previousStockOnHand);
    assertFalse(captured.getIsDraft());
  }

  private PhysicalInventory createInventoryDraft(
      UUID orderableId, UUID programId, UUID facilityId) {
    PhysicalInventory inventory = createInventoryDto(programId, facilityId)
        .toPhysicalInventoryForDraft();
    inventory.getLineItems().get(0).setOrderableId(orderableId);
    return inventory;
  }

  private void shouldSearchBasedOnIsDraft(PhysicalInventory inventory, boolean isDraft) {
    inventory.setIsDraft(isDraft);
    when(physicalInventoryRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, isDraft))
        .thenReturn(Collections.singletonList(inventory));

    List<PhysicalInventoryDto> foundDraft =
        physicalInventoryService.findPhysicalInventory(programId, facilityId, isDraft);

    assertEquals(1, foundDraft.size());
    assertEquals(programId, foundDraft.get(0).getProgramId());
    assertEquals(facilityId, foundDraft.get(0).getFacilityId());
    assertEquals(isDraft, foundDraft.get(0).getIsDraft());

    PhysicalInventoryLineItemDto lineItemDto = foundDraft.get(0).getLineItems().get(0);
    assertEquals(orderableId, lineItemDto.getOrderableId());
    assertNull(lineItemDto.getQuantity());
  }

  private PhysicalInventoryDto createInventoryDto(UUID programId, UUID facilityId) {
    PhysicalInventoryLineItemDto piLineItemDto = PhysicalInventoryLineItemDto
        .builder()
        .orderableId(randomUUID())
        .quantity(null)//quantity is allowed to be null
        .build();
    return PhysicalInventoryDto
        .builder()
        .id(UUID.randomUUID())
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
    assertThat(lineItem.getOrderableId(), is(lineItemDto.getOrderableId()));
  }
}