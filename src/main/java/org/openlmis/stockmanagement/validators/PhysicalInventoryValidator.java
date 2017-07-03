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
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("PhysicalInventoryValidator")
public class PhysicalInventoryValidator {
  private static final String USE_VVM = "useVVM";
  private static final String VVM_STATUS = "vvmStatus";

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Check for physical inventory dto's validity.
   * Throws {@link ValidationMessageException} if an error found.
   * @param inventory physical inventory to validate.
   */
  public void validate(PhysicalInventoryDto inventory) {
    List<PhysicalInventoryLineItemDto> lineItems = inventory.getLineItems();

    if (isEmpty(lineItems)) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING);
    }

    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderable() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING);
    }

    validateVvmStatuses(lineItems);
  }

  private void validateVvmStatuses(List<PhysicalInventoryLineItemDto> lineItems) {
    Map<UUID, OrderableDto> orderables = lineItems
        .stream()
        .map(PhysicalInventoryLineItemDto::getOrderableId)
        .distinct()
        .collect(Collectors.toMap(
            Function.identity(),
            id -> orderableReferenceDataService.findOne(id)
        ));

    for (PhysicalInventoryLineItemDto item : lineItems) {
      OrderableDto orderable = orderables.get(item.getOrderableId());

      boolean useVvm = false;
      boolean hasVvmStatus = false;

      if (orderable.getExtraData() != null) {
        useVvm = Boolean.parseBoolean(orderable.getExtraData().get(USE_VVM));
      }

      if (item.getExtraData() != null) {
        hasVvmStatus = item.getExtraData().get(VVM_STATUS) != null;
      }

      if (!useVvm && hasVvmStatus) {
        throw new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_ORDERABLE_DISABLED_VVM);
      }
    }
  }
}
