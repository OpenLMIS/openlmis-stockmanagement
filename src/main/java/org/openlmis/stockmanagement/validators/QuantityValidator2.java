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

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.dto.StockEventLineItem;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component(value = "QuantityValidator2")
public class QuantityValidator2 implements StockEventValidator2 {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto2 stockEventDto)
      throws IllegalAccessException, InstantiationException {
    LOGGER.debug("Validate quantity");
    boolean isDebitEvent = stockEventDto.hasDestination() || hasDebitReason(stockEventDto);
    if (!isDebitEvent || isEmpty(stockEventDto.getLineItems())) {
      return;
    }

    for (StockEventLineItem lineItem : stockEventDto.getLineItems()) {
      validateQuantity(stockEventDto, lineItem);
    }
  }

  private void validateQuantity(StockEventDto2 eventDto, StockEventLineItem lineItem)
      throws IllegalAccessException, InstantiationException {
    StockCard foundCard =
        tryFindCard(eventDto.getProgramId(), eventDto.getFacilityId(), lineItem);

    //create line item from event line item and add it to stock card for recalculation
    StockCardLineItem itemToBe = calculateStockOnHand(eventDto, lineItem, foundCard);
    foundCard.getLineItems().forEach(item -> {
      if (item.getStockOnHand() < 0) {
        throwQuantityError(lineItem.getQuantity());
      }
    });
    //remove the line item from card to avoid jpa persistence
    foundCard.getLineItems().remove(itemToBe);
  }

  private StockCard tryFindCard(UUID programId, UUID facilityId, StockEventLineItem lineItem) {
    UUID orderableId = lineItem.getOrderableId();
    StockCard foundCard = stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableId(programId, facilityId, orderableId);
    if (foundCard == null) {
      //first movement should not be debit
      throwQuantityError(lineItem.getQuantity());
    }
    return foundCard;
  }

  private void throwQuantityError(Integer quantity) {
    throw new ValidationMessageException(
        new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, quantity));
  }

  private StockCardLineItem calculateStockOnHand(StockEventDto2 eventDto,
                                                 StockEventLineItem lineItemDto,
                                                 StockCard foundCard)
      throws InstantiationException, IllegalAccessException {
    StockCardLineItem itemToBe = StockCardLineItem
        .createLineItemFrom2(eventDto, lineItemDto, foundCard, null, null);

    foundCard.calculateStockOnHand();
    return itemToBe;
  }

  private boolean hasDebitReason(StockEventDto2 stockEventDto) {
    if (stockEventDto.hasReason()) {
      StockCardLineItemReason reason = reasonRepository.findOne(stockEventDto.getReasonId());
      return reason != null && reason.isDebitReasonType();
    }
    return false;
  }
}
