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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//commenting the following line out, so spring will NOT pick it up and inject it
//this is for jira ticket 1987
//@Component(value = "QuantityValidator")
public class QuantityValidator implements StockEventValidator {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {
    LOGGER.debug("Validate quantity");
    boolean isDebitEvent = stockEventDto.hasDestination() || hasDebitReason(stockEventDto);
    if (!isDebitEvent) {
      return;
    }

    StockCard foundCard = findCard(stockEventDto);

    //the next line will create line item from event and add it to stock card for recalculation
    List<StockCardLineItem> itemsToBe = calculateStockOnHand(stockEventDto, foundCard);
    foundCard.getLineItems().forEach(item -> {
      if (item.getStockOnHand() < 0) {
        throwQuantityError(stockEventDto.getQuantity());
      }
    });
    //next line will remove the line item from card to avoid jpa persistence
    foundCard.getLineItems().remove(itemsToBe.get(0));
  }

  private StockCard findCard(StockEventDto stockEventDto) {
    StockCard foundCard = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableId(
        stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(),
        stockEventDto.getOrderableId());

    if (foundCard == null) {
      throwQuantityError(stockEventDto.getQuantity());
    }
    return foundCard;
  }

  private void throwQuantityError(Integer quantity) {
    throw new ValidationMessageException(
        new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, quantity));
  }

  private List<StockCardLineItem> calculateStockOnHand(StockEventDto eventDto, StockCard foundCard)
      throws InstantiationException, IllegalAccessException {
    List<StockCardLineItem> itemsToBe = StockCardLineItem
        .createLineItemsFrom(eventDto, foundCard, null, null);
    foundCard.calculateStockOnHand();
    return itemsToBe;
  }

  private boolean hasDebitReason(StockEventDto stockEventDto) {
    if (stockEventDto.hasReason()) {
      StockCardLineItemReason reason = reasonRepository.findOne(stockEventDto.getReasonId());
      return reason != null && reason.isDebitReasonType();
    }
    return false;
  }
}
