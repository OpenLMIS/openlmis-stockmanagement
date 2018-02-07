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
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1 This validator makes sure stock on hand does NOT go below zero for any stock card. 2 This
 * validator also makes sure soh does not be over upper limit of integer. It does so by
 * re-calculating soh of each orderable/lot combo. The re-calculation does not apply to physical
 * inventory. This has a negative impact on performance. The impact grows larger as stock card line
 * items accumulates over time. Because re-calculation requires reading stock card line items from
 * DB.
 */
@Component(value = "QuantityValidator")
public class QuantityValidator implements StockEventValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(QuantityValidator.class);

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate quantity");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    Map<OrderableLotIdentity, List<StockEventLineItemDto>> sameOrderableGroups = stockEventDto
        .getLineItems()
        .stream()
        .collect(groupingBy(OrderableLotIdentity::identityOf));

    for (List<StockEventLineItemDto> group : sameOrderableGroups.values()) {
      // increase may cause int overflow, decrease may cause below zero
      validateEventItems(stockEventDto, group);
    }
  }

  private void validateEventItems(StockEventDto event, List<StockEventLineItemDto> items) {
    StockCard foundCard = tryFindCard(event, items.get(0));

    if (event.isPhysicalInventory()) {
      validateQuantities(event, items, foundCard.getStockOnHand());
    }

    // create line item from event line item and add it to stock card for recalculation
    calculateStockOnHand(event, items, foundCard);
  }

  private StockCard tryFindCard(StockEventDto event, StockEventLineItemDto lineItem) {
    StockCard foundCard = event.getContext().findCard(OrderableLotIdentity.identityOf(lineItem));

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

  private void validateQuantities(StockEventDto event, List<StockEventLineItemDto> items,
                                  Integer stockOnHand) {
    for (StockEventLineItemDto item : items) {
      Integer quantity = item.getQuantity();
      if (stockOnHand != null && quantity != null) {
        List<StockEventAdjustmentDto> adjustments = item.getStockAdjustments();
        if (isNotEmpty(adjustments)) {
          validateStockAdjustments(adjustments);
        }
      }
    }
  }

  /**
   * Make sure each stock adjustment a non-negative quantity assigned.
   *
   * @param adjustments adjustments to validate
   */
  private void validateStockAdjustments(List<StockEventAdjustmentDto> adjustments) {
    // Check for valid quantities
    boolean hasNegative = adjustments
        .stream()
        .mapToInt(StockEventAdjustmentDto::getQuantity)
        .anyMatch(quantity -> quantity < 0);

    if (hasNegative) {
      throw new ValidationMessageException(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);
    }
  }

  private void calculateStockOnHand(StockEventDto eventDto, List<StockEventLineItemDto> group,
                                    StockCard foundCard) {
    for (StockEventLineItemDto lineItem : group) {
      StockCardLineItem stockCardLineItem = StockCardLineItem
          .createLineItemFrom(eventDto, lineItem, foundCard, null);
      stockCardLineItem.setReason(eventDto.getContext().findEventReason(lineItem.getReasonId()));
    }

    foundCard.calculateStockOnHand();
  }

  private void debugAdjustments(StockEventDto event, List<StockEventAdjustmentDto> adjustments) {
    if (LOGGER.isDebugEnabled() && isNotEmpty(adjustments)) {
      LOGGER.debug("Logging adjustments");
      for (StockEventAdjustmentDto adj : adjustments) {
        StockCardLineItemReason reason = event.getContext().findEventReason(adj.getReasonId());

        // we check if reason exists in ReasonExistenceValidator
        if (null == reason) {
          continue;
        }

        LOGGER.debug("Adjustment {}: {}", reason.getName(), adj.getQuantity());
      }
    }
  }
}
