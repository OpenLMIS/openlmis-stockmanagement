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

package org.openlmis.stockmanagement.service;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERRRO_EVENT_SOH_EXCEEDS_LIMIT;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The type Stock card line item service.
 */
@Service
public class StockCardLineItemService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(StockCardLineItemService.class);

  /**
   * Populate stock on hand line items.
   *
   * @param stockCard the stockCard
   */
  public void populateStockOnHandLineItems(StockCard stockCard) {
    int previousSoH = 0;

    if (isNotEmpty(stockCard.getLineItems())) {
      stockCard.reorderLineItems();
      for (StockCardLineItem lineItem : stockCard.getLineItems()) {
        previousSoH = populateStockOnHandLineItems(lineItem, previousSoH);
      }
    }
  }

  private Integer populateStockOnHandLineItems(StockCardLineItem stockCardLineItem,
      int previousSoH) {
    if (stockCardLineItem.isPhysicalInventory()) {
      stockCardLineItem.setReason(
          determineStockCardLineItemReasonByQuantity(stockCardLineItem, previousSoH));
      stockCardLineItem.setStockOnHand(stockCardLineItem.getQuantity());
      stockCardLineItem.setQuantity(Math.abs(stockCardLineItem.getStockOnHand() - previousSoH));
      LOGGER.debug("Physical inventory: {}", stockCardLineItem.getStockOnHand());
      return stockCardLineItem.getStockOnHand();
    } else if (stockCardLineItem.isPositive()) {
      return tryIncrease(stockCardLineItem, previousSoH);
    } else {
      return tryDecrease(stockCardLineItem, previousSoH);
    }
  }

  private Integer tryDecrease(StockCardLineItem stockCardLineItem, int previousSoH) {
    try {
      int difference = Math.subtractExact(previousSoH, stockCardLineItem.getQuantity());
      LOGGER.debug("try decrease soh: {} - {} = {}", previousSoH, stockCardLineItem.getQuantity(),
          difference);
      stockCardLineItem.setStockOnHand(difference);
    } catch (ArithmeticException ex) {
      throw new ValidationMessageException(
          new Message(ERRRO_EVENT_SOH_EXCEEDS_LIMIT, previousSoH, stockCardLineItem.getQuantity(),
              ex));
    }

    return stockCardLineItem.getStockOnHand();
  }

  private Integer tryIncrease(StockCardLineItem stockCardLineItem, int previousSoH) {
    try {
      int sum = Math.addExact(previousSoH, stockCardLineItem.getQuantity());
      LOGGER.debug("try increase soh: {} + {} = {}", previousSoH, stockCardLineItem.getQuantity(),
          sum);
      stockCardLineItem.setStockOnHand(sum);
    } catch (ArithmeticException ex) {
      throw new ValidationMessageException(
          new Message(ERRRO_EVENT_SOH_EXCEEDS_LIMIT, previousSoH, stockCardLineItem.getQuantity(),
              ex));
    }
    return stockCardLineItem.getStockOnHand();
  }

  private StockCardLineItemReason determineStockCardLineItemReasonByQuantity(
      StockCardLineItem stockCardLineItem,
      int previousSoH) {
    if (stockCardLineItem.getQuantity() > previousSoH) {
      return StockCardLineItemReason.physicalCredit();
    } else if (stockCardLineItem.getQuantity() < previousSoH) {
      return StockCardLineItemReason.physicalDebit();
    } else {
      return StockCardLineItemReason.physicalBalance();
    }
  }
}
