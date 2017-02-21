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

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED;

@Component(value = "FreeTextValidator")
public class FreeTextValidator implements StockEventValidator {

  @Autowired
  private NodeRepository nodeRepository;

  @Autowired
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {
    checkSourceDestinationFreeTextBothPresent(stockEventDto);
    if (stockEventDto.hasSource()) {
      checkSourceFreeText(stockEventDto);
    }
    if (stockEventDto.hasDestination()) {
      checkDestinationFreeText(stockEventDto);
    }
    if (stockEventDto.hasReason()) {
      checkReasonFreeText(stockEventDto);
    }
  }

  private void checkSourceDestinationFreeTextBothPresent(StockEventDto stockEventDto) {
    if (stockEventDto.hasSourceFreeText() && stockEventDto.hasDestinationFreeText()) {
      throwError(ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT,
          stockEventDto.getSourceFreeText(), stockEventDto.getDestinationFreeText());
    }
  }

  private void checkSourceFreeText(StockEventDto stockEventDto) {
    checkNodeFreeText(stockEventDto.getSourceId(), ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED,
        stockEventDto.getSourceFreeText());
  }

  private void checkDestinationFreeText(StockEventDto stockEventDto) {
    checkNodeFreeText(stockEventDto.getDestinationId(), ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED,
        stockEventDto.getDestinationFreeText());
  }

  private void checkNodeFreeText(UUID nodeId, String errorKey, String freeText) {
    Node node = nodeRepository.findOne(nodeId);
    if (null != node && node.isRefDataFacility() && freeText != null) {
      throwError(errorKey, nodeId, freeText);
    }
  }

  private void checkReasonFreeText(StockEventDto stockEventDto) {
    StockCardLineItemReason reason =
        stockCardLineItemReasonRepository.findOne(stockEventDto.getReasonId());

    if (null != reason && !reason.getIsFreeTextAllowed() && stockEventDto.hasReasonFreeText()) {
      throwError(ERROR_REASON_FREE_TEXT_NOT_ALLOWED, stockEventDto.getReasonId(),
          stockEventDto.getReasonFreeText());
    }
  }

  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
