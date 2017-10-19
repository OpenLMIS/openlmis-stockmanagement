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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * This validator checks reason free text, source free text and destination free text. Reason free
 * text is only allowed when reason id is present and the reason allows free text. Same case for
 * source free text and destination free text.
 */
@Component(value = "FreeTextValidator")
public class FreeTextValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate free text");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    for (StockEventLineItem eventLineItem : stockEventDto.getLineItems()) {
      checkSourceDestinationFreeTextBothPresent(eventLineItem);

      checkNodeFreeText(
          stockEventDto, eventLineItem.getSourceId(),
          eventLineItem.getSourceFreeText(),
          ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED
      );
      checkNodeFreeText(
          stockEventDto, eventLineItem.getDestinationId(),
          eventLineItem.getDestinationFreeText(),
          ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED
      );

      checkReasonFreeText(stockEventDto, eventLineItem);
    }
  }

  private void checkSourceDestinationFreeTextBothPresent(StockEventLineItem eventLineItem) {
    if (eventLineItem.hasSourceFreeText() && eventLineItem.hasDestinationFreeText()) {
      throwError(ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT,
          eventLineItem.getSourceFreeText(), eventLineItem.getDestinationFreeText());
    }
  }

  private void checkNodeFreeText(StockEventDto event, UUID nodeId,
                                 String freeText, String errorKey) {
    if (nodeId != null) {
      Node node = event.getContext().findNode(nodeId);

      if (null != node && node.isRefDataFacility() && freeText != null) {
        throwError(errorKey, nodeId, freeText);
      }
    } else if (freeText != null) {
      //node free text exist but node id is null
      throwError(errorKey, nodeId, freeText);
    }
  }

  private void checkReasonFreeText(StockEventDto event, StockEventLineItem lineItem) {
    if (!lineItem.hasReasonFreeText()) {
      return;//if there is no reason free text, then there is no need to validate
    }

    boolean reasonNotAllowFreeText = lineItem.hasReasonId()
        && !isFreeTextAllowed(event, lineItem);
    boolean hasNoReasonIdButHasFreeText = !lineItem.hasReasonId();
    if (reasonNotAllowFreeText || hasNoReasonIdButHasFreeText) {
      throwError(ERROR_REASON_FREE_TEXT_NOT_ALLOWED,
          lineItem.getReasonId(), lineItem.getReasonFreeText());
    }
  }

  private boolean isFreeTextAllowed(StockEventDto event, StockEventLineItem lineItem) {
    StockCardLineItemReason reason = event.getContext().findEventReason(lineItem.getReasonId());
    return reason != null && reason.getIsFreeTextAllowed();
  }

  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
