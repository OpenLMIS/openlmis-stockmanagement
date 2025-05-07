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

package org.openlmis.stockmanagement.validators;

import static java.util.UUID.randomUUID;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryValidatorTest {

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private VvmValidator vvmValidator;

  @Mock
  private PhysicalInventoriesRepository repository;

  @InjectMocks
  private PhysicalInventoryValidator validator;

  @Test
  public void shouldPassAndCallVvmValidator() {
    // given
    PhysicalInventoryDto inventory = newInventory();

    doNothing()
        .when(vvmValidator).validate(any(), anyString(), eq(false));

    // when
    validator.validateDraft(inventory, UUID.fromString(inventory.getId().toString()));

    // then
    verify(vvmValidator, atLeastOnce())
        .validate(eq(inventory.getLineItems()), anyString(), eq(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenNoLineItemsPresent() {
    // given
    PhysicalInventoryDto inventory = newInventory();
    inventory.setLineItems(Collections.emptyList());

    // when
    validator.validateDraft(inventory, inventory.getId());

    // then
    verify(vvmValidator, atLeastOnce())
        .validate(eq(inventory.getLineItems()), anyString(), eq(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenLineItemHasNoOrderable() {
    // given
    PhysicalInventoryDto inventory = newInventory();
    inventory.setLineItems(Collections.singletonList(new PhysicalInventoryLineItemDto()));

    // when
    validator.validateDraft(inventory, inventory.getId());

    // then
    verify(vvmValidator, atLeastOnce())
        .validate(eq(inventory.getLineItems()), anyString(), eq(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenIdMismatch() {
    PhysicalInventoryDto inventory = newInventory();

    validator.validateDraft(inventory, randomUUID());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenInventoryIsSubmitted() {
    testValidateBasedOnIfExistingIsDraft(false);
  }

  @Test
  public void shouldPassWhenInventoryIsDraft() {
    testValidateBasedOnIfExistingIsDraft(true);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenEmptyDraftHasNoFacility() {
    PhysicalInventoryDto physicalInventoryDto = new PhysicalInventoryDto();
    physicalInventoryDto.setProgramId(UUID.randomUUID());

    validator.validateEmptyDraft(physicalInventoryDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectWhenEmptyDraftHasNoProgram() {
    PhysicalInventoryDto physicalInventoryDto = new PhysicalInventoryDto();
    physicalInventoryDto.setFacilityId(UUID.randomUUID());

    validator.validateEmptyDraft(physicalInventoryDto);
  }

  private void testValidateBasedOnIfExistingIsDraft(boolean isDraft) {
    PhysicalInventoryDto inventory = newInventory();
    PhysicalInventory existingInventory = mock(PhysicalInventory.class);
    when(existingInventory.getIsDraft()).thenReturn(isDraft);
    when(repository.findById(inventory.getId())).thenReturn(Optional.of(existingInventory));

    validator.validateDraft(inventory, inventory.getId());
  }

  private PhysicalInventoryDto newInventory() {
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setId(randomUUID());
    inventory.setLineItems(Collections.singletonList(generateLineItem()));
    return inventory;
  }

  private PhysicalInventoryLineItemDto generateLineItem() {
    return PhysicalInventoryLineItemDto
        .builder()
        .orderableId(UUID.randomUUID())
        .stockOnHand(5)
        .quantity(5)
        .stockAdjustments(new ArrayList<>())
        .build();
  }
}
