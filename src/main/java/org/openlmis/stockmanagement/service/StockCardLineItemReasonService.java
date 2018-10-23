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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_CATEGORY_CHANGED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_CATEGORY_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ID_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_NAME_DUPLICATE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_NAME_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TYPE_CHANGED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TYPE_MISSING;

import java.util.Objects;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockCardLineItemReasonService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(StockCardLineItemReasonService.class);

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  /**
   * Save or update stock card line item reason.
   *
   * @param reason reason DTO object
   * @return created reason DTO object
   */
  public StockCardLineItemReason saveOrUpdate(StockCardLineItemReason reason) {
    validateRequiredValueNotNull(reason);
    validateReasonNameDuplicate(reason);
    verifyInvariants(reason);
    LOGGER.debug("Is going to save reason");
    return reasonRepository.save(reason);
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

  private void verifyInvariants(StockCardLineItemReason reason) {
    if (null == reason.getId()) {
      // a new reason does not have invariants
      return;
    }

    StockCardLineItemReason foundReason = reasonRepository.findOne(reason.getId());

    if (null == foundReason) {
      // a new reason but with the given id
      return;
    }

    if (!Objects.equals(reason.getReasonType(), foundReason.getReasonType())) {
      throwException(ERROR_LINE_ITEM_REASON_TYPE_CHANGED);
    }

    if (!Objects.equals(reason.getReasonCategory(), foundReason.getReasonCategory())) {
      throwException(ERROR_LINE_ITEM_REASON_CATEGORY_CHANGED);
    }
  }

  /**
   * Check try to update reason to be duplicate with other one.
   * Throw exception without update itself.
   *
   * @param reason would be updated
   */
  private void validateReasonNameDuplicate(StockCardLineItemReason reason) {
    StockCardLineItemReason foundReason = reasonRepository.findByName(reason.getName());
    if (foundReason != null) {
      boolean isUpdatingItself = foundReason.getId() == reason.getId();
      if (isUpdatingItself) {
        return;
      }
      throwException(ERROR_LINE_ITEM_REASON_NAME_DUPLICATE);
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
