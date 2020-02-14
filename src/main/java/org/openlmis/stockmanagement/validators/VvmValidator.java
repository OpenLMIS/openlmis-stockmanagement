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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_INVALID;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This validator ensures that line items for orderables
 * with disabled VVM usage do not specify VVM Status.
 */
@Component("VvmValidator")
public class VvmValidator {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(StockEventValidator.class);

  private static final String USE_VVM = "useVVM";
  private static final String VVM_STATUS = "vvmStatus";

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Validates whether the vvm applicables have proper vvm status (if applicable).
   * Throws ValidationMessageException if any of items is in invalid state.
   *  @param vvmApplicables list of items to test
   * @param messageKey error message key for exception
   * @param ignoreMissingOrderable whether should
   */
  public void validate(List<? extends VvmApplicable> vvmApplicables, String messageKey,
                       boolean ignoreMissingOrderable) {
    XLOGGER.entry(vvmApplicables);
    Profiler profiler = new Profiler("STOCK_EVENT_VVM_VALIDATOR");
    profiler.setLogger(XLOGGER);

    Set<UUID> orderableIds = vvmApplicables
        .stream()
        .map(VvmApplicable::getOrderableId)
        .collect(Collectors.toSet());

    profiler.start("FIND_ORDERABLES_BY_IDS");
    List<OrderableDto> orderables = orderableReferenceDataService.findByIds(orderableIds);

    Map<UUID, OrderableDto> groupById = orderables
        .stream()
        .collect(Collectors.toMap(OrderableDto::getId, orderable -> orderable));

    profiler.start("CHECK_VVM_APPLICABLE_ITEMS");
    for (VvmApplicable item : vvmApplicables) {
      OrderableDto orderable = groupById.get(item.getOrderableId());

      if (null == orderable) {
        if (ignoreMissingOrderable) {
          continue;
        } else {
          throw new ValidationMessageException(ERROR_EVENT_ORDERABLE_INVALID);
        }
      }

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

    profiler.stop().log();
    XLOGGER.exit(vvmApplicables);
  }
}
