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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_DISABLED_VVM;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;
import static org.springframework.util.CollectionUtils.isEmpty;

import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This validator ensures that physical inventory line items for orderables
 * with disabled VVM usage do not specify VVM Status.
 */
@Component("PhysicalInventoryValidator")
public class PhysicalInventoryValidator extends VvmValidator {

  /**
    * Check for physical inventory dto's validity.
    * Throws {@link ValidationMessageException} if an error found.
    * @param inventory physical inventory to validate.
    */
  public void validate(PhysicalInventoryDto inventory)
      throws InstantiationException, IllegalAccessException {
    List<PhysicalInventoryLineItemDto> lineItems = inventory.getLineItems();

    if (isEmpty(lineItems)) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING);
    }

    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderable() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING);
    }

    validateVvm(lineItems, ERROR_PHYSICAL_INVENTORY_ORDERABLE_DISABLED_VVM);
  }
}
