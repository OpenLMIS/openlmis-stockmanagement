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
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.physicalBalance;
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.physicalCredit;
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.physicalDebit;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERRRO_EVENT_SOH_EXCEEDS_LIMIT;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/*
 * StockOnHandCalculationService should NOT be used.
 * Instead please, use {@link org.openlmis.stockmanagement.service.CalculatedStockOnHandService}.
 * Removing this logic is described in task OLMIS-6779.
 */
public class StockOnHandCalculationService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(StockOnHandCalculationService.class);

  @Autowired
  private OrderableReferenceDataService orderableService;

  /**
   * Returns stock card with updated Stock on Hand value.
   *
   * @param card containing line items
   * @return stock card with updated SOH value
   * @throws ValidationMessageException thrown when SoH is below 0 or arithmetic error occurred
   */
  public StockCard calculateStockOnHand(StockCard card) {
    int soh = 0;

    if (isNotEmpty(card.getLineItems())) {
      for (StockCardLineItem lineItem : card.getSortedLineItems()) {
        soh = calculateStockOnHand(lineItem, soh);
      }
    }

    LOGGER.debug("Calculated stock on hand: {}", soh);
    card.setStockOnHand(soh);
    return card;
  }

  private Integer calculateStockOnHand(StockCardLineItem item, int prevSoH) {
    if (item.isPhysicalInventory()) {
      item.setReason(determineReasonByQuantity(item, prevSoH));
      item.setStockOnHand(item.getQuantity());
      item.setQuantity(Math.abs(item.getStockOnHand() - prevSoH));
      LOGGER.debug("Physical inventory: {}", item.getStockOnHand());
      return item.getStockOnHand();
    } else if (shouldIncrease(item)) {
      return tryIncrease(item, prevSoH);
    } else {
      return tryDecrease(item, prevSoH);
    }
  }

  private Integer tryDecrease(StockCardLineItem item, int prevSoH) {
    if (prevSoH - item.getQuantity() < 0) {
      throwQuantityExceedException(item, prevSoH);
    }

    int difference = (prevSoH - item.getQuantity());
    LOGGER.debug("try decrease soh: {} - {} = {}", prevSoH, item.getQuantity(), difference);
    item.setStockOnHand(difference);

    return item.getStockOnHand();
  }

  private Integer tryIncrease(StockCardLineItem item, int prevSoH) {
    try {
      int sum = Math.addExact(prevSoH, item.getQuantity());
      LOGGER.debug("try increase soh: {} + {} = {}", prevSoH, item.getQuantity(), sum);
      item.setStockOnHand(sum);
    } catch (ArithmeticException ex) {
      throw new ValidationMessageException(
          new Message(ERRRO_EVENT_SOH_EXCEEDS_LIMIT, prevSoH, item.getQuantity(), ex));
    }
    return item.getStockOnHand();
  }

  private StockCardLineItemReason determineReasonByQuantity(StockCardLineItem item, int prevSoH) {
    if (item.getQuantity() > prevSoH) {
      return physicalCredit();
    } else if (item.getQuantity() < prevSoH) {
      return physicalDebit();
    } else {
      return physicalBalance();
    }
  }

  private boolean shouldIncrease(StockCardLineItem item) {
    return item.isPositive();
  }

  private void throwQuantityExceedException(StockCardLineItem item, int prevSoH) {
    OrderableDto orderable = orderableService.findOne(item.getStockCard().getOrderableId());
    String code = (orderable != null) ? orderable.getProductCode() : "";
    throw new ValidationMessageException(
        new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH,
            item.getOccurredDate(), code, prevSoH, item.getQuantity()));
  }
}
