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
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

@Component(value = "QuantityValidator")
public class QuantityValidator implements StockEventValidator {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {
    boolean isDebitEvent = stockEventDto.hasDestination() || hasDebitReasonOnEvent(stockEventDto);
    if (!isDebitEvent) {
      return;
    }

    if (stockEventDto.getQuantity() > currentStockOnHand(stockEventDto)) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, stockEventDto.getQuantity()));
    }
  }

  private Integer currentStockOnHand(StockEventDto stockEventDto) {
    StockCard foundCard = stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableId(stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(), stockEventDto.getOrderableId());
    if (foundCard == null) {
      return 0;
    }
    foundCard.calculateStockOnHand();
    return foundCard.getStockOnHand();
  }

  private boolean hasDebitReasonOnEvent(StockEventDto stockEventDto) {
    if (stockEventDto.hasReason()) {
      StockCardLineItemReason reason = reasonRepository.findOne(stockEventDto.getReasonId());
      return reason != null && reason.isDebitReasonType();
    }
    return false;
  }
}
