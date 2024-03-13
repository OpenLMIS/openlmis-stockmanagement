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

import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.service.notifier.HighStockNotifier;
import org.openlmis.stockmanagement.service.notifier.LowStockNotifier;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private LowStockNotifier lowStockNotifier;

  @Autowired
  private HighStockNotifier highStockNotifier;

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  @Autowired
  private RightReferenceDataService rightReferenceDataService;

  /**
  * From the stock event, check each line item's stock card and see if stock on hand has gone to
  * zero. If so, send a notification to all of that stock card's editors.
  * 
  * @param eventDto the stock event to process
  */
  public void callAllNotifications(StockEventDto eventDto) {
    RightDto right = rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT);
    eventDto
        .getLineItems()
        .forEach(line -> callNotifications(eventDto, line, right.getId()));
  }

  private void callNotifications(StockEventDto event, StockEventLineItemDto eventLine,
      UUID rightId) {
    XLOGGER.entry(event, eventLine);
    Profiler profiler = new Profiler("CALL_NOTIFICATION_FOR_LINE_ITEM");
    profiler.setLogger(XLOGGER);

    profiler.start("COPY_STOCK_CARD");
    OrderableLotIdentity identity = OrderableLotIdentity.identityOf(eventLine);
    StockCard stockCard = event.getContext().findCard(identity);
    
    // Check if stockCard is null
    if (stockCard == null) {
      XLOGGER.error("StockCard not found for identity: {}", identity);
      return;
    }

    int averageConsumption = (int) Math.ceil(averageConsumption(stockCard));
    Integer aggregateStockOnHand = aggregateStockOnHand(stockCard);

    if (aggregateStockOnHand == 0) {
      stockoutNotifier.notifyStockEditors(stockCard, rightId);
    } else if (aggregateStockOnHand < averageConsumption) {
      lowStockNotifier.notifyStockEditors(stockCard, rightId, averageConsumption);
    } else if (aggregateStockOnHand > (3 * averageConsumption)) {
      highStockNotifier.notifyStockEditors(stockCard, rightId, averageConsumption);
    }

    profiler.stop().log();
    XLOGGER.exit();
  }

  private double averageConsumption(StockCard stockCard) {
    // Get the stock card aggregates for the past three months
    Map<UUID, StockCardAggregate> stockCardsMap = stockCardSummariesService.getGroupedStockCards(
        stockCard.getProgramId(), 
        stockCard.getFacilityId(),
        Collections.singleton(stockCard.getOrderableId()),
        LocalDate.now().minusMonths(3),
        LocalDate.now());

    // Retrieve the aggregate for the current stock card's orderable ID
    StockCardAggregate aggregate = stockCardsMap.get(stockCard.getOrderableId());

    if (aggregate == null) {
        return 0; // No data available for this stock card
    }

    // Get the total consumed amount
    int totalConsumed = aggregate.getAmount("consumed", LocalDate.now().minusMonths(3), LocalDate.now());
    totalConsumed = totalConsumed * -1;

    // Calculate the number of days in the past three months
    long daysInPeriod = ChronoUnit.DAYS.between(LocalDate.now().minusMonths(3), LocalDate.now());

    // Get the total stockout days in the past three months
    long stockOutDays = aggregate.getStockoutDays(LocalDate.now().minusMonths(3), LocalDate.now());

    // Calculate the number of days when the stock was available
    long availableDays = daysInPeriod - stockOutDays;

    // Calculate the average monthly consumption
    // Note: If there were no available days (to avoid division by zero), return 0
    return availableDays > 0 ? (double) totalConsumed / availableDays * 30 : 0;
  }

  private Integer aggregateStockOnHand(StockCard stockCard) {
    // Get the stock card aggregates for the past three months
    Map<UUID, StockCardAggregate> stockCardsMap = stockCardSummariesService.getGroupedStockCards(
        stockCard.getProgramId(), 
        stockCard.getFacilityId(),
        Collections.singleton(stockCard.getOrderableId()),
        LocalDate.now().minusMonths(3),
        LocalDate.now());

    // Retrieve the aggregate for the current stock card's orderable ID
    StockCardAggregate aggregate = stockCardsMap.get(stockCard.getOrderableId());

    if (aggregate == null) {
        return 0; // No data available for this stock card
    }

    return aggregate.getTotalStockOnHand();
  }

}
 