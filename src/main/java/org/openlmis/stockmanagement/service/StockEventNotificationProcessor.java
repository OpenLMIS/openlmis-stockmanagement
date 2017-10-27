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

import java.util.concurrent.Future;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 * A service that helps the StockEventProcessor to determine notification.
 */
@Service
public class StockEventNotificationProcessor {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(
      StockEventNotificationProcessor.class);

  @Autowired
  private StockoutNotifier stockoutNotifier;

  /**
   * From the stock event, check each line item's stock card and see if stock on hand has gone to
   * zero. If so, send a notification to all of that stock card's editors. This is done 
   * asynchronously.
   * 
   * @param eventDto the stock event to process
   * @return a null future
   */
  @Async
  public Future<Void> callAllNotifications(StockEventDto eventDto) {
    eventDto
        .getLineItems()
        .forEach(line -> callNotifications(eventDto, line));

    return new AsyncResult<>(null);
  }

  private void callNotifications(StockEventDto event, StockEventLineItem eventLine) {
    XLOGGER.entry(event, eventLine);
    Profiler profiler = new Profiler("CALL_NOTIFICATION_FOR_LINE_ITEM");
    profiler.setLogger(XLOGGER);

    profiler.start("COPY_STOCK_CARD");
    OrderableLotIdentity identity = OrderableLotIdentity.identityOf(eventLine);
    StockCard card = event.getContext().findCard(identity);
    StockCard copy = card.shallowCopy();

    for (StockCardLineItem line : copy.getLineItems()) {
      StockCardLineItemReason reason = line.getReason();

      if (null != reason) {
        line.setReason(event.getContext().findCardReason(reason.getId()));
      }
    }

    profiler.start("CALCULATE_STOCK_ON_HAND");
    copy.calculateStockOnHand();

    profiler.start("NOTIFY_STOCK_CARD_EDITORS");
    if (copy.getStockOnHand() == 0) {
      stockoutNotifier.notifyStockEditors(copy);
    }

    profiler.stop().log();
    XLOGGER.exit();
  }
}
