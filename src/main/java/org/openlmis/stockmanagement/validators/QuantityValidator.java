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

import static java.util.stream.Collectors.groupingBy;
import static org.openlmis.stockmanagement.domain.adjustment.ReasonCategory.PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.util.OrderableLotIdentity;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component(value = "QuantityValidator")
public class QuantityValidator implements StockEventValidator {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {
    LOGGER.debug("Validate quantity");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    Map<OrderableLotIdentity, List<StockEventLineItem>> sameOrderableGroups = stockEventDto
        .getLineItems().stream()
        .collect(groupingBy(StockEventLineItem::orderableAndLotIdentity));

    for (List<StockEventLineItem> group : sameOrderableGroups.values()) {
      boolean anyDebitInGroup = group.stream().anyMatch(this::hasDebitReason);
      if (stockEventDto.hasDestination() || anyDebitInGroup) {
        validateQuantity(stockEventDto, group);
      }
    }
  }

  private void validateQuantity(StockEventDto stockEventDto, List<StockEventLineItem> group)
      throws InstantiationException, IllegalAccessException {
    StockCard foundCard =
        tryFindCard(stockEventDto.getProgramId(), stockEventDto.getFacilityId(), group.get(0));

    //create line item from event line item and add it to stock card for recalculation
    List<StockCardLineItem> itemsToBe = calculateStockOnHand(stockEventDto, group, foundCard);
    foundCard.getLineItems().forEach(item -> {
      if (item.getStockOnHand() < 0) {
        throwQuantityError(group);
      }
    });
    avoidPersistence(foundCard, itemsToBe);
  }

  private void avoidPersistence(StockCard foundCard, List<StockCardLineItem> itemsToBe) {
    //during recalculating soh, physical inventory line items will be assigned a reason
    //those reasons does not exist in DB, they are from physicalCredit and physicalDebit methods
    //remove their reason assignment after recalculation to avoid jpa persistence
    foundCard.getLineItems().stream()
        .filter(lineItem -> lineItem.getReason().getReasonCategory() == PHYSICAL_INVENTORY)
        .forEach(lineItem -> lineItem.setReason(null));
    //remove the line items from card to avoid jpa persistence
    foundCard.getLineItems().removeAll(itemsToBe);
  }

  private StockCard tryFindCard(UUID programId, UUID facilityId, StockEventLineItem lineItem) {
    StockCard foundCard = stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableIdAndLotId(programId, facilityId,
            lineItem.getOrderableId(), lineItem.getLotId());
    if (foundCard == null) {
      foundCard = new StockCard();
      foundCard.setLineItems(new ArrayList<>());
    }
    return foundCard;
  }

  private void throwQuantityError(List<StockEventLineItem> group) {
    throw new ValidationMessageException(
        new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, group));
  }

  private List<StockCardLineItem> calculateStockOnHand(StockEventDto eventDto,
                                                       List<StockEventLineItem> group,
                                                       StockCard foundCard)
      throws InstantiationException, IllegalAccessException {
    List<StockCardLineItem> lineItemsToBe = new ArrayList<>();
    for (StockEventLineItem lineItem : group) {
      StockCardLineItem stockCardLineItem = StockCardLineItem
          .createLineItemFrom(eventDto, lineItem, foundCard, null, null);
      stockCardLineItem.setReason(findReason(lineItem.getReasonId()));
      lineItemsToBe.add(stockCardLineItem);
    }

    foundCard.calculateStockOnHand();
    return lineItemsToBe;
  }

  private StockCardLineItemReason findReason(UUID reasonId) {
    if (reasonId != null) {
      return reasonRepository.findOne(reasonId);
    }
    return null;
  }

  private boolean hasDebitReason(StockEventLineItem lineItem) {
    StockCardLineItemReason reason = findReason(lineItem.getReasonId());
    return reason != null && reason.isDebitReasonType();
  }
}
