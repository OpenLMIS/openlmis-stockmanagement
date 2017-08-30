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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 1 This validator makes sure stock on hand does NOT go below zero for any stock card.
 * 2 This validator also makes sure soh does not be over upper limit of integer.
 * It does so by re-calculating soh of each orderable/lot combo.
 * The re-calculation does not apply to physical inventory.
 * This has a negative impact on performance. The impact grows larger as stock card line items
 * accumulates over time. Because re-calculation requires reading stock card line items from DB.
 */
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
        .collect(groupingBy(OrderableLotIdentity::identityOf));

    for (List<StockEventLineItem> group : sameOrderableGroups.values()) {
      // increase may cause int overflow, decrease may cause below zero
      validateEventItems(stockEventDto, group);
    }
  }

  private void validateEventItems(StockEventDto event, List<StockEventLineItem> items)
      throws IllegalAccessException, InstantiationException {
    StockCard foundCard = tryFindCard(
        event.getProgramId(),
        event.getFacilityId(),
        items.get(0)
    );

    if (event.isPhysicalInventory()) {
      validateQuantities(items, foundCard.getStockOnHand());
    }

    // create line item from event line item and add it to stock card for recalculation
    calculateStockOnHand(event, items, foundCard);
  }

  private StockCard tryFindCard(UUID programId, UUID facilityId, StockEventLineItem lineItem) {
    StockCard foundCard = stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableIdAndLotId(programId, facilityId,
            lineItem.getOrderableId(), lineItem.getLotId());
    if (foundCard == null) {
      StockCard emptyCard = new StockCard();
      emptyCard.setLineItems(new ArrayList<>());
      return emptyCard;
    } else {
      //use a shallow copy of stock card to do recalculation, because some domain model will be
      //modified during recalculation, this will avoid persistence of those modified models
      StockCard stockCard = foundCard.shallowCopy();
      stockCard.calculateStockOnHand();
      return stockCard;
    }
  }

  private StockCardLineItemReason findReason(UUID reasonId) {
    if (reasonId != null) {
      return reasonRepository.findOne(reasonId);
    }
    return null;
  }

  private void validateQuantities(List<StockEventLineItem> items, Integer stockOnHand)
      throws InstantiationException, IllegalAccessException {
    for (StockEventLineItem item : items) {
      Integer quantity = item.getQuantity();
      if (stockOnHand != null && quantity != null) {
        List<StockAdjustment> adjustments = item.getStockAdjustments();

        int adjustmentsQuantity = 0;

        if (adjustments != null && !adjustments.isEmpty()) {
          validateStockAdjustments(adjustments);
          adjustmentsQuantity = adjustments
              .stream()
              .mapToInt(StockAdjustment::getSignedQuantity)
              .sum();
        } else if (!stockOnHand.equals(quantity)) {
          throw new ValidationMessageException(
              ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED);
        }

        if (stockOnHand + adjustmentsQuantity != quantity) {
          throw new ValidationMessageException(
              ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER);
        }
      }
    }
  }

  /**
   * Make sure each stock adjustment a non-negative quantity assigned.
   * @param adjustments adjustments to validate
   */
  private void validateStockAdjustments(List<StockAdjustment> adjustments) {
    // Check for valid quantities
    boolean hasNegative = adjustments
            .stream()
            .mapToInt(StockAdjustment::getQuantity)
            .anyMatch(quantity -> quantity < 0);

    if (hasNegative) {
      throw new ValidationMessageException(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);
    }
  }

  private void calculateStockOnHand(
      StockEventDto eventDto, List<StockEventLineItem> group, StockCard foundCard)
      throws InstantiationException, IllegalAccessException {
    for (StockEventLineItem lineItem : group) {
      StockCardLineItem stockCardLineItem = StockCardLineItem
          .createLineItemFrom(eventDto, lineItem, foundCard, null, null);
      stockCardLineItem.setReason(findReason(lineItem.getReasonId()));
    }

    foundCard.calculateStockOnHand();
  }
}
