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

package org.openlmis.stockmanagement.web;

import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_DUPLICATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class PhysicalInventoryController {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  /**
   * Create physical inventory.
   *
   * @param dto physical inventory dto.
   * @return the created physical inventory's id.
   */
  @Transactional(rollbackFor = {InstantiationException.class, IllegalAccessException.class})
  @RequestMapping(value = "physicalInventories", method = POST)
  public ResponseEntity<UUID> createPhysicalInventory(@RequestBody PhysicalInventoryDto dto)
      throws IllegalAccessException, InstantiationException {
    permissionService.canCreateStockEvent(dto.getProgramId(), dto.getFacilityId());

    if (!dto.getIsDraft()) {
      return new ResponseEntity<>(submitPhysicalInventory(dto), CREATED);
    }
    return null;
  }

  private UUID submitPhysicalInventory(PhysicalInventoryDto physicalInventoryDto)
      throws InstantiationException, IllegalAccessException {
    validate(physicalInventoryDto);

    PhysicalInventory inventory = physicalInventoryDto.toPhysicalInventory();
    for (StockEventDto eventDto : physicalInventoryDto.toEventDtos()) {
      UUID savedEventId = stockEventProcessor.process(eventDto);
      inventory.getStockEvents().add(fromId(savedEventId, StockEvent.class));
    }
    return physicalInventoriesRepository.save(inventory).getId();
  }

  private void validate(PhysicalInventoryDto physicalInventoryDto) {
    List<PhysicalInventoryLineItemDto> lineItems = physicalInventoryDto.getLineItems();
    if (lineItems == null || lineItems.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING));
    }
    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderableDto() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING));
    }
    checkOrderableDuplication(lineItems);
  }

  private void checkOrderableDuplication(List<PhysicalInventoryLineItemDto> lineItems) {
    long count = lineItems.stream()
        .map(lineItem -> lineItem.getOrderableDto().getId()).distinct().count();
    if (count < lineItems.size()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_ORDERABLE_DUPLICATION));
    }
  }

}
