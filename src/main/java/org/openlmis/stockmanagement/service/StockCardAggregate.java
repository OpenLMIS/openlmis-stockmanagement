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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that aggregates stock cards that can fulfill single Orderable.
 * This class provides a set of methods to calculate stock out days
 * and values assigned to reason tags using all Stock Cards from list.
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class StockCardAggregate {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardAggregate.class);

  @Getter
  @Setter
  private List<StockCard> stockCards;

  @Getter
  @Setter
  private List<CalculatedStockOnHand> calculatedStockOnHands;

  /**
   * Returns amount of products assigned to reasons that have given tag.
   * It takes into consideration reason type.
   *
   * @param tag used for filtering stock card line items by reason tag
   * @param startDate used for filtering stock card line items by occurred date
   * @param endDate used for filtering stock card line items by occurred date
   * @return quantity value, is negative for Debit reason
   */
  public Integer getAmount(String tag, LocalDate startDate, LocalDate endDate) {
    List<StockCardLineItem> filteredLineItems = filterLineItems(startDate, endDate, tag);

    return isEmpty(filteredLineItems) ? 0 : filterLineItems(startDate, endDate, tag).stream()
        .mapToInt(StockCardLineItem::getQuantityWithSign)
        .sum();
  }

  /**
   * Returns map of tags found in reasons from all stock card line items
   * and accumulated value of line items that have reason with given tag.
   *
   * @param startDate used for filtering stock card line items by occurred date
   * @param endDate used for filtering stock card line items by occurred date
   * @return map of tags and amounts from connected line items
   */
  public Map<String, Integer> getAmounts(LocalDate startDate, LocalDate endDate) {
    List<StockCardLineItem> filteredLineItems = filterLineItems(startDate, endDate, null);

    return isEmpty(filteredLineItems) ? new HashMap<>() : filteredLineItems.stream()
        .map(lineItem -> {
          int value = lineItem.getQuantityWithSign();
          List<String> tags = null == lineItem.getReason()
              ? emptyList()
              : lineItem.getReason().getTags();

          return tags.stream()
              .map(tag -> new ImmutablePair<>(tag, value))
              .collect(toList());
        })
        .flatMap(Collection::stream)
        .collect(toMap(
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            Integer::sum));
  }

  /**
   * Returns a number of days where stock was below zero in range of dates.
   *
   * @param startDate used for filtering stock card line items by occurred date
   * @param endDate used for filtering stock card line items by occurred date
   * @return number of days without stock available
   */
  public Long getStockoutDays(LocalDate startDate, LocalDate endDate) {
    Map<LocalDate, Integer> stockOnHands = calculatedStockOnHands.stream()
        .collect(toMap(
            CalculatedStockOnHand::getOccurredDate,
            CalculatedStockOnHand::getStockOnHand,
            Integer::sum,
            TreeMap::new));

    long stockOutDays;
    if (startDate == null || endDate == null) {
      stockOutDays = calculateStockoutDays(
          getStockoutPeriods(stockOnHands, endDate), startDate, endDate);
    } else {

      int sumOfStockOnHandDuringPeriod = 0;
      for (Map.Entry<LocalDate, Integer> entry : stockOnHands.entrySet()) {
        if (isAfterOrEqual(startDate, entry.getKey()) && isBeforeOrEqual(endDate, entry.getKey())) {
          sumOfStockOnHandDuringPeriod += entry.getValue();
        }
      }
      if (sumOfStockOnHandDuringPeriod == 0) {
        long daysBetween = DAYS.between(startDate, endDate) + 1;
        // According to OLMIS project specification month length can be maximum 30 days.
        if (daysBetween >= 28) {
          daysBetween -= 1;
        }
        stockOutDays = daysBetween;
      } else {
        stockOutDays = calculateStockoutDays(
            getStockoutPeriods(stockOnHands, endDate), startDate, endDate);
      }
    }
    return stockOutDays;
  }

  private List<StockCardLineItem> filterLineItems(LocalDate startDate,
      LocalDate endDate, String tag) {

    return stockCards.stream()
        .flatMap(stockCard -> stockCard.getLineItems().stream())
        .filter(lineItem ->
            isBeforeOrEqual(lineItem.getOccurredDate(), startDate)
                && isAfterOrEqual(lineItem.getOccurredDate(), endDate)
                && (null == tag || lineItem.containsTag(tag)))
        .collect(toList());
  }

  private Map<LocalDate, LocalDate> getStockoutPeriods(
      Map<LocalDate, Integer> stockOnHands, LocalDate endDate) {
    LocalDate stockOutStartDate = null;
    Map<LocalDate, LocalDate> stockOutDaysMap = new TreeMap<>();

    for (Entry<LocalDate, Integer> stockOnHandEntry : stockOnHands.entrySet()) {
      if (stockOnHandEntry.getValue() <= 0) {
        stockOutStartDate = stockOnHandEntry.getKey();
      } else if (null != stockOutStartDate) {
        LOGGER.debug("stock out days from {} to {}", stockOutStartDate, stockOnHandEntry.getKey());
        stockOutDaysMap.put(stockOutStartDate, stockOnHandEntry.getKey());
        stockOutStartDate = null;
      }
    }

    if (null != stockOutStartDate) {
      LOGGER.debug("stock out days from {} to {}", null == endDate ? LocalDate.now() : endDate);
      stockOutDaysMap.put(stockOutStartDate, null == endDate ? LocalDate.now() : endDate);
    }

    return stockOutDaysMap;
  }

  private long calculateStockoutDays(Map<LocalDate, LocalDate> stockOutDaysMap,
      LocalDate startDate, LocalDate endDate) {

    return stockOutDaysMap.isEmpty() ? 0 : stockOutDaysMap.keySet().stream()
        .filter(key -> isAfterOrEqual(key, endDate))
        .filter(key -> isBeforeOrEqual(stockOutDaysMap.get(key), startDate))
        .peek(key ->
            LOGGER.debug("filtered stock out days from {} to {}", key, stockOutDaysMap.get(key)))
        .mapToLong(key -> DAYS.between(
            !isBeforeOrEqual(key, startDate)
                ? startDate
                : key,
            !isAfterOrEqual(stockOutDaysMap.get(key), endDate)
                ? endDate.plusDays(1)
                : stockOutDaysMap.get(key)))
        .sum();
  }

  private boolean isBeforeOrEqual(LocalDate date, LocalDate dateToCompare) {
    return null == dateToCompare || !dateToCompare.isAfter(date);
  }

  private boolean isAfterOrEqual(LocalDate date, LocalDate dateToCompare) {
    return null == dateToCompare || !dateToCompare.isBefore(date);
  }
}
