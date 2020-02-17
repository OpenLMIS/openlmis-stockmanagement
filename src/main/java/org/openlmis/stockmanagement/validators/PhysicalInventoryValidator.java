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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ID_MISMATCH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_DISABLED_VVM;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;
import static org.slf4j.ext.XLoggerFactory.getXLogger;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.slf4j.ext.XLogger;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This validator ensures that physical inventory line items for orderables
 * with disabled VVM usage do not specify VVM Status.
 */
@Component("PhysicalInventoryValidator")
public class PhysicalInventoryValidator {

  private static final XLogger XLOGGER = getXLogger(PhysicalInventoryValidator.class);

  @Autowired
  private VvmValidator vvmValidator;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  /**
    * Check for physical inventory dto's validity.
    * Throws {@link ValidationMessageException} if an error found.
    * @param inventory physical inventory to validate.
    */
  public void validateDraft(PhysicalInventoryDto inventory, UUID id) {
    XLOGGER.entry(inventory);
    Profiler profiler = new Profiler("PHYSICAL_INVENTORY_VALIDATOR");
    profiler.setLogger(XLOGGER);

    if (!inventory.getId().equals(id)) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_ID_MISMATCH);
    }

    profiler.start("FIND_PHYSICAL_INVENTORY_BY_ID");
    PhysicalInventory foundInventory = physicalInventoriesRepository.findOne(id);
    if (foundInventory != null && !foundInventory.getIsDraft()) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED);
    }

    List<PhysicalInventoryLineItemDto> lineItems = inventory.getLineItems();

    profiler.start("VALIDATE_LINE_ITEMS");
    validateLineItems(lineItems);
    vvmValidator.validate(lineItems, ERROR_PHYSICAL_INVENTORY_ORDERABLE_DISABLED_VVM, false);

    profiler.stop().log();
    XLOGGER.exit(inventory);
  }

  /**
    * Check for physical inventory dto's validity.
    * Throws {@link ValidationMessageException} if an error found.
    * @param inventory physical inventory to validate.
    */
  public void validateEmptyDraft(PhysicalInventoryDto inventory) {
    validateNotNull(inventory.getProgramId(), ERROR_PROGRAM_ID_MISSING);
    validateNotNull(inventory.getFacilityId(), ERROR_FACILITY_ID_MISSING);
  }

  private void validateLineItems(List<PhysicalInventoryLineItemDto> lineItems) {
    if (isEmpty(lineItems)) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING);
    }

    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderableId() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING);
    }
  }

  private void validateNotNull(Object field, String errorMessage) {
    if (field == null) {
      throw new ValidationMessageException(errorMessage);
    }
  }
}
