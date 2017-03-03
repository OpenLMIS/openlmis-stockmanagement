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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_CATEGORY_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ID_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_NAME_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TYPE_MISSING;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StockCardLineItemReasonService {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Save or update stock card line item reason.
   *
   * @param reason reason DTO object
   * @return created reason DTO object
   */
  public StockCardLineItemReason saveOrUpdate(StockCardLineItemReason reason) {
    permissionService.canManageReasons();
    validateRequiredValueNotNull(reason);
    return reasonRepository.save(reason);
  }

  /**
   * Check if has same reason in DB.
   *
   * @param reason to be checked
   * @return a boolean for same reason existing
   */
  public boolean reasonExists(StockCardLineItemReason reason) {
    StockCardLineItemReason foundReason = reasonRepository
        .findByNameAndReasonTypeAndReasonCategoryAndIsFreeTextAllowedAndDescription(
            reason.getName(),
            reason.getReasonType(),
            reason.getReasonCategory(),
            reason.getIsFreeTextAllowed(),
            reason.getDescription());

    return foundReason != null;
  }

  /**
   * Check if the would be updated reason exists.
   *
   * @param reasonId would be updated reason's ID
   */
  public void checkUpdateReasonIdExists(UUID reasonId) {
    if (reasonRepository.findOne(reasonId) == null) {
      throw new ValidationMessageException(new Message(ERROR_LINE_ITEM_REASON_ID_NOT_FOUND));
    }
  }

  private void validateRequiredValueNotNull(StockCardLineItemReason reason) {
    if (reason.hasNoName()) {
      throwException(ERROR_LINE_ITEM_REASON_NAME_MISSING);
    } else if (reason.hasNoType()) {
      throwException(ERROR_LINE_ITEM_REASON_TYPE_MISSING);
    } else if (reason.hasNoCategory()) {
      throwException(ERROR_LINE_ITEM_REASON_CATEGORY_MISSING);
    } else if (reason.hasNoIsFreeTextAllowed()) {
      throwException(ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING);
    }
  }

  private void throwException(String errorKey) {
    throw new ValidationMessageException(new Message(errorKey));
  }
}
