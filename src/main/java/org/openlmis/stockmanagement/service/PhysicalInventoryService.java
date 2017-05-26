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

import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;
import static org.springframework.util.CollectionUtils.isEmpty;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PhysicalInventoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalInventoryService.class);

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  /**
   * Persist physical inventory, with an event id. (For now, we only save physical inventory, but we
   * are not reading it anywhere in the code. We do this in case in the future some countries may
   * wanna view all the physical inventories that has been done in the past.)
   *
   * @param inventoryDto inventoryDto.
   * @param eventId      eventId.
   * @throws IllegalAccessException IllegalAccessException.
   * @throws InstantiationException InstantiationException.
   */
  public void submitPhysicalInventory(PhysicalInventoryDto inventoryDto, UUID eventId)
      throws IllegalAccessException, InstantiationException {
    LOGGER.info("submit physical inventory");
    deleteExistingDraft(inventoryDto);

    PhysicalInventory inventory = inventoryDto.toPhysicalInventoryForSubmit();
    inventory.setStockEvent(fromId(eventId, StockEvent.class));

    physicalInventoriesRepository.save(inventory);
  }

  /**
   * Find draft by program and facility.
   *
   * @param programId  programId.
   * @param facilityId facilityId.
   * @return found draft, or if not found, returns empty draft.
   */
  public PhysicalInventoryDto findDraft(UUID programId, UUID facilityId) {
    LOGGER.info("find physical inventory draft");
    PhysicalInventory foundInventory = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true);
    if (foundInventory == null) {
      return null;
    } else {
      return PhysicalInventoryDto.from(foundInventory);
    }
  }

  /**
   * Save or update draft.
   *
   * @param dto physical inventory dto.
   * @return the saved inventory.
   */
  public PhysicalInventoryDto saveDraft(PhysicalInventoryDto dto) {
    LOGGER.info("save physical inventory draft");
    validateLineItems(dto);
    deleteExistingDraft(dto);

    physicalInventoriesRepository.save(dto.toPhysicalInventoryForDraft());
    return dto;
  }

  /**
   * Delete draft.
   *
   * @param dto physical inventory dto.
   */
  public void deleteExistingDraft(PhysicalInventoryDto dto) {
    PhysicalInventory foundInventory = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(dto.getProgramId(), dto.getFacilityId(), true);
    if (foundInventory != null) {
      physicalInventoriesRepository.delete(foundInventory);
    }
  }

  private void validateLineItems(PhysicalInventoryDto dto) {
    List<PhysicalInventoryLineItemDto> lineItems = dto.getLineItems();
    if (isEmpty(lineItems)) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING));
    }
    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderable() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING));
    }
  }
}
