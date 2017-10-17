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

import static java.util.stream.Collectors.toMap;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_DRAFT_EXISTS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_FOUND;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.openlmis.stockmanagement.validators.PhysicalInventoryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PhysicalInventoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalInventoryService.class);

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Autowired
  private PhysicalInventoryValidator physicalInventoryValidator;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @Autowired
  private StockCardRepository stockCardRepository;

  /**
   * Find draft by program and facility.
   *
   * @param programId  programId.
   * @param facilityId facilityId.
   * @return found draft, or if not found, returns empty draft.
   */
  public List<PhysicalInventoryDto> findPhysicalInventory(UUID programId,
                                                          UUID facilityId,
                                                          Boolean isDraft) {
    LOGGER.info("find physical inventory draft");
    List<PhysicalInventory> found;
    if (isDraft == null) {
      found = physicalInventoriesRepository
          .findByProgramIdAndFacilityId(programId, facilityId);
    } else {
      found = physicalInventoriesRepository
          .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, isDraft);
    }
    if (found == null) {
      return Collections.emptyList();
    }
    return PhysicalInventoryDto.from(found);
  }

  /**
   * Create new draft.
   *
   * @param dto physical inventory dto.
   * @return the saved inventory.
   */
  public PhysicalInventoryDto createNewDraft(PhysicalInventoryDto dto) {
    LOGGER.info("create physical inventory draft");
    physicalInventoryValidator.validateEmptyDraft(dto);
    checkPermission(dto.getProgramId(), dto.getFacilityId());

    checkIfDraftExists(dto);

    dto.setId(null);
    PhysicalInventory save = physicalInventoriesRepository.save(dto.toEmptyPhysicalInventory());
    dto.setId(save.getId());

    return dto;
  }

  /**
   * Save or update draft.
   *
   * @param dto physical inventory dto.
   * @return the saved inventory.
   */
  public PhysicalInventoryDto saveDraft(PhysicalInventoryDto dto, UUID id) {
    LOGGER.info("save physical inventory draft");
    physicalInventoryValidator.validateDraft(dto, id);
    checkPermission(dto.getProgramId(), dto.getFacilityId());

    checkIfDraftExists(dto, id);

    physicalInventoriesRepository.save(dto.toPhysicalInventoryForDraft());
    return dto;
  }

  /**
   * Delete draft.
   *
   * @param id physical inventory id.
   */
  public void deletePhysicalInventory(UUID id) {
    PhysicalInventory foundInventory = physicalInventoriesRepository.findOne(id);
    if (foundInventory != null) {
      checkPermission(foundInventory.getProgramId(), foundInventory.getFacilityId());
      if (!foundInventory.getIsDraft()) {
        throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED);
      }
      physicalInventoriesRepository.delete(foundInventory);
    } else {
      throw new ResourceNotFoundException(new Message(ERROR_PHYSICAL_INVENTORY_NOT_FOUND, id));
    }
  }

  /**
   * Checks permission.
   */
  public void checkPermission(UUID program, UUID facility) {
    homeFacilityPermissionService.checkProgramSupported(program);
    permissionService.canEditPhysicalInventory(program, facility);
  }

  /**
   * Persist physical inventory, with an event id.
   *
   * @param inventoryDto inventoryDto.
   * @param eventId      eventId.
   */
  void submitPhysicalInventory(PhysicalInventoryDto inventoryDto, UUID eventId) {
    LOGGER.info("submit physical inventory");

    PhysicalInventory inventory = inventoryDto.toPhysicalInventoryForSubmit();

    if (null != eventId) {
      StockEvent event = new StockEvent();
      event.setId(eventId);

      inventory.setStockEvent(event);
    }

    Map<OrderableLotIdentity, StockCard> cards = stockCardRepository
        .findByProgramIdAndFacilityId(inventory.getProgramId(), inventory.getFacilityId())
        .stream()
        .collect(toMap(OrderableLotIdentity::identityOf, card -> card));

    for (PhysicalInventoryLineItem line : inventory.getLineItems()) {
      StockCard foundCard = cards.get(OrderableLotIdentity.identityOf(line));
      //use a shallow copy of stock card to do recalculation, because some domain model will be
      //modified during recalculation, this will avoid persistence of those modified models
      if (foundCard != null) {
        StockCard stockCard = foundCard.shallowCopy();
        stockCard.calculateStockOnHand();
        line.setPreviousStockOnHandWhenSubmitted(stockCard.getStockOnHand());
      }
    }

    physicalInventoriesRepository.save(inventory);
  }

  private void checkIfDraftExists(PhysicalInventoryDto dto) {
    List<PhysicalInventory> found = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(dto.getProgramId(), dto.getFacilityId(), true);
    if (found != null && !found.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_DRAFT_EXISTS,
              dto.getProgramId(),
              dto.getFacilityId()));
    }
  }

  private void checkIfDraftExists(PhysicalInventoryDto dto, UUID physicalInventoryToUpdate) {
    List<PhysicalInventory> found = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(dto.getProgramId(), dto.getFacilityId(), true);
    if (found != null
        && !found.isEmpty()
        && physicalInventoryToUpdate != found.get(0).getId()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_DRAFT_EXISTS,
              dto.getProgramId(),
              dto.getFacilityId()));
    }
  }
}
