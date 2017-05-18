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

import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.TRANSFER;
import static org.openlmis.stockmanagement.domain.reason.ReasonType.CREDIT;
import static org.openlmis.stockmanagement.domain.reason.ReasonType.DEBIT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ISSUE_REASON_TYPE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * This validator makes sure that:
 * If source and reason are both present, then reason must be credit.
 * If destination and reason are both present, then reason must be debit.
 * And reason must be of TRANSFER category for both cases.
 * It's ok for source and destination to appear without a reason accompanied though.
 */
@Component(value = "ReceiveIssueReasonValidator")
public class ReceiveIssueReasonValidator implements StockEventValidator {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Override
  public void validate(StockEventDto eventDto) {
    LOGGER.debug("Validate receive and issue reason");

    if (!eventDto.hasLineItems()) {
      return;
    }

    eventDto.getLineItems().forEach(eventLineItem -> {
      if (eventLineItem.hasSourceId()) {
        checkReceiveReason(eventLineItem);
      }
      if (eventLineItem.hasDestinationId()) {
        checkIssueReason(eventLineItem);
      }
    });
  }

  private void checkReceiveReason(StockEventLineItem eventLineItem) {
    checkReason(eventLineItem, CREDIT,
        ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID,
        ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID);
  }

  private void checkIssueReason(StockEventLineItem stockEventLineItem) {
    checkReason(stockEventLineItem, DEBIT,
        ERROR_EVENT_ISSUE_REASON_TYPE_INVALID,
        ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID);
  }

  private void checkReason(StockEventLineItem lineItem, ReasonType expectedReasonType,
                           String typeErrorKey, String categoryErrorKey) {
    if (lineItem.hasReasonId()) {
      StockCardLineItemReason foundReason = reasonRepository.findOne(lineItem.getReasonId());
      //this validator does not care if reason id points to something in DB
      //that is handled by other validators
      if (foundReason != null) {
        checkReasonType(expectedReasonType, typeErrorKey, lineItem.getReasonId(), foundReason);
        checkReasonCategory(categoryErrorKey, lineItem.getReasonId(), foundReason);
      }
    }
  }

  private void checkReasonType(ReasonType expectedReasonType, String typeErrorKey,
                               UUID reasonId, StockCardLineItemReason foundReason) {
    ReasonType reasonType = foundReason.getReasonType();
    if (reasonType != expectedReasonType) {
      throw new ValidationMessageException(new Message(typeErrorKey, reasonId, reasonType));
    }
  }

  private void checkReasonCategory(String categoryErrorKey, UUID reasonId,
                                   StockCardLineItemReason foundReason) {
    ReasonCategory reasonCategory = foundReason.getReasonCategory();
    if (reasonCategory != TRANSFER) {
      throw new ValidationMessageException(new Message(categoryErrorKey, reasonId, reasonCategory));
    }
  }
}
