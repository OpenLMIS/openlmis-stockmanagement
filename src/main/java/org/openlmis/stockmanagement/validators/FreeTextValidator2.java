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

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component(value = "FreeTextValidator2")
public class FreeTextValidator2 implements StockEventValidator2 {

  @Autowired
  private NodeRepository nodeRepository;

  @Autowired
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @Override
  public void validate(StockEventDto2 stockEventDto) {
    LOGGER.debug("Validate free text");
    checkSourceDestinationFreeTextBothPresent(stockEventDto);

    checkNodeFreeText(stockEventDto.getSourceId(), stockEventDto.getSourceFreeText(),
        ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED);
    checkNodeFreeText(stockEventDto.getDestinationId(), stockEventDto.getDestinationFreeText(),
        ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED);

    checkReasonFreeText(stockEventDto);
  }

  private void checkSourceDestinationFreeTextBothPresent(StockEventDto2 stockEventDto) {
    if (stockEventDto.hasSourceFreeText() && stockEventDto.hasDestinationFreeText()) {
      throwError(ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT,
          stockEventDto.getSourceFreeText(), stockEventDto.getDestinationFreeText());
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

  private void checkReasonFreeText(StockEventDto2 stockEventDto) {
    UUID reasonId = stockEventDto.getReasonId();
    String freeText = stockEventDto.getReasonFreeText();
    if (stockEventDto.hasReason()) {
      StockCardLineItemReason reason = stockCardLineItemReasonRepository.findOne(reasonId);

      if (null != reason && !reason.getIsFreeTextAllowed() && stockEventDto.hasReasonFreeText()) {
        throwError(ERROR_REASON_FREE_TEXT_NOT_ALLOWED, reasonId, freeText);
      }
    } else if (freeText != null) {
      //reason free text exist but reason id is null
      throwError(ERROR_REASON_FREE_TEXT_NOT_ALLOWED, reasonId, freeText);
    }
  }

  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
