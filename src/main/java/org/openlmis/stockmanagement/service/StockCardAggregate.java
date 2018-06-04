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
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byOccurredDate;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byProcessedDate;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItemComparators.byReasonPriority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Class that aggregates stock cards that can fulfill single Orderable.
 * This class provides a set of methods to calculate stock out days
 * and values assigned to reason tags using all Stock Cards from list.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class StockCardAggregate {

  @Getter
  @Setter
  private List<StockCard> stockCards;

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

    return isEmpty(filteredLineItems) ? null : filterLineItems(startDate, endDate, tag).stream()
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
    List<StockCardLineItem> lineItems = stockCards.stream()
        .flatMap(stockCard -> stockCard.getLineItems().stream())
        .sorted(getComparator())
        .collect(toList());

    return calculateStockoutDays(getStockoutPeriods(lineItems), startDate, endDate);
  }

  private Comparator<StockCardLineItem> getComparator() {
    return byOccurredDate()
        .thenComparing(byProcessedDate())
        .thenComparing(byReasonPriority());
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

  private Map<LocalDate, LocalDate> getStockoutPeriods(List<StockCardLineItem> lineItems) {

    int recentSoh = 0;
    LocalDate stockOutDateStart = null;
    Map<LocalDate, LocalDate> stockOutDaysMap = new TreeMap<>();

    for (StockCardLineItem lineItem : lineItems) {
      recentSoh = recentSoh + lineItem.getQuantityWithSign();
      lineItem.setStockOnHand(recentSoh);
      if (recentSoh <= 0) {
        stockOutDateStart = lineItem.getOccurredDate();
      } else if (null != stockOutDateStart) {
        stockOutDaysMap.put(stockOutDateStart, lineItem.getOccurredDate());
        stockOutDateStart = null;
      }
    }

    return stockOutDaysMap;
  }

  private Long calculateStockoutDays(Map<LocalDate, LocalDate> stockOutDaysMap,
      LocalDate startDate, LocalDate endDate) {

    return stockOutDaysMap.isEmpty() ? null : stockOutDaysMap.keySet().stream()
        .filter(key -> isAfterOrEqual(key, endDate))
        .filter(key -> isBeforeOrEqual(stockOutDaysMap.get(key), startDate))
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
