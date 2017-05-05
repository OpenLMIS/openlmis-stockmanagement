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
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component(value = "FreeTextValidator")
public class FreeTextValidator implements StockEventValidator {

  @Autowired
  private NodeRepository nodeRepository;

  @Autowired
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate free text");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    stockEventDto.getLineItems().forEach(eventLineItem -> {
      checkSourceDestinationFreeTextBothPresent(eventLineItem);

      checkNodeFreeText(eventLineItem.getSourceId(), eventLineItem.getSourceFreeText(),
          ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED);
      checkNodeFreeText(eventLineItem.getDestinationId(), eventLineItem.getDestinationFreeText(),
          ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED);

      checkReasonFreeText(eventLineItem);
    });
  }

  private void checkSourceDestinationFreeTextBothPresent(StockEventLineItem eventLineItem) {
    if (eventLineItem.hasSourceFreeText() && eventLineItem.hasDestinationFreeText()) {
      throwError(ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT,
          eventLineItem.getSourceFreeText(), eventLineItem.getDestinationFreeText());
    }
  }

  private void checkNodeFreeText(UUID nodeId, String freeText, String errorKey) {
    if (nodeId != null) {
      Node node = nodeRepository.findOne(nodeId);
      if (null != node && node.isRefDataFacility() && freeText != null) {
        throwError(errorKey, nodeId, freeText);
      }
    } else if (freeText != null) {
      //node free text exist but node id is null
      throwError(errorKey, nodeId, freeText);
    }
  }

  private void checkReasonFreeText(StockEventLineItem lineItem) {
    String freeText = lineItem.getReasonFreeText();
    if (lineItem.hasReason()) {
      StockCardLineItemReason reason = stockCardLineItemReasonRepository
          .findOne(lineItem.getReasonId());

      if (null != reason && !reason.getIsFreeTextAllowed() && lineItem.hasReasonFreeText()) {
        throwError(ERROR_REASON_FREE_TEXT_NOT_ALLOWED, lineItem.getReasonId(), freeText);
      }
    } else if (freeText != null) {
      //reason free text exist but reason id is null
      throwError(ERROR_REASON_FREE_TEXT_NOT_ALLOWED, lineItem.getReasonId(), freeText);
    }
  }

  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
