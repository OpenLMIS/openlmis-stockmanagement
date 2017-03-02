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

import static java.util.stream.StreamSupport.stream;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_CATEGORY_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_NAME_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TYPE_NOT_FOUND;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockCardLineItemReasonService {

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
    boolean hasSameReason = stream(reasonRepository.findAll().spliterator(), false)
        .anyMatch(foundReason -> foundReason.equals(reason));

    if (hasSameReason) {
      return reason;
    }

    return reasonRepository.save(reason);
  }

  private void validateRequiredValueNotNull(StockCardLineItemReason reason) {
    if (reason.hasNoName()) {
      throwException(ERROR_LINE_ITEM_REASON_NAME_NOT_FOUND);
    } else if (reason.hasNoType()) {
      throwException(ERROR_LINE_ITEM_REASON_TYPE_NOT_FOUND);
    } else if (reason.hasNoCategory()) {
      throwException(ERROR_LINE_ITEM_REASON_CATEGORY_NOT_FOUND);
    } else if (reason.hasNoIsFreeTextAllowed()) {
      throwException(ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_NOT_FOUND);
    }
  }

  private void throwException(String errorKey) {
    throw new ValidationMessageException(new Message(errorKey));
  }
}
