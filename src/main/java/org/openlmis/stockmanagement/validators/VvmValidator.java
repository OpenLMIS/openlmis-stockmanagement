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

import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This validator ensures that line items for orderables
 * with disabled VVM usage do not specify VVM Status.
 */
public class VvmValidator {
  private static final String USE_VVM = "useVVM";
  private static final String VVM_STATUS = "vvmStatus";

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Validates whether the vvm applicables have proper vvm status (if applicable).
   * Throws ValidationMessageException if any of items is in invalid state.
   *
   * @param vvmApplicables list of items to test
   * @param messageKey error message key for exception
   */
  public void validate(List<? extends VvmApplicable> vvmApplicables, String messageKey)
      throws IllegalAccessException, InstantiationException {
    Map<UUID, OrderableDto> orderables = vvmApplicables
        .stream()
        .map(VvmApplicable::getOrderableId)
        .distinct()
        .collect(Collectors.toMap(
            Function.identity(),
            id -> orderableReferenceDataService.findOne(id)
        ));

    for (VvmApplicable item : vvmApplicables) {
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
        throw new ValidationMessageException(messageKey);
      }
    }
  }
}
