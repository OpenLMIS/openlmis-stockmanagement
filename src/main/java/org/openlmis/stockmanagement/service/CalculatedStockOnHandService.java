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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculatedStockOnHandService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalculatedStockOnHandService.class);

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Autowired
  private OrderableReferenceDataService orderableService;

  /**
   * Returns list of stock cards with fetched Stock on Hand values.
   *
   * @param programId  program id to find stock cards
   * @param facilityId facility id to find stock cards
   * @param asOfDate   date used to get latest stock on hand before or equal specific date
   * @return List of stock cards with SOH values, empty list if no stock cards were found.
   */
  public List<StockCard> getStockCardsWithStockOnHand(
      UUID programId, UUID facilityId, LocalDate asOfDate) {
    List<StockCard> stockCards =
        stockCardRepository.findByProgramIdAndFacilityId(programId, facilityId);

    if (null == stockCards) {
      return Collections.emptyList();
    }

    stockCards.forEach(stockCard ->
        fetchStockOnHand(stockCard, asOfDate != null ? asOfDate : LocalDate.now()));

    return stockCards;
  }

  /**
   * Returns list of stock cards with fetched Stock on Hand values.
   *
   * @param programId  program id to find stock cards
   * @param facilityId facility id to find stock cards
   * @return List of stock cards with SOH values, empty list if no stock cards were found.
   */
  public List<StockCard> getStockCardsWithStockOnHand(UUID programId, UUID facilityId) {
    return getStockCardsWithStockOnHand(programId, facilityId, null);
  }

  /**
   * Returns list of stock cards with fetched Stock on Hand values.
   *
   * @param programId    program id to find stock card
   * @param facilityId   facility id to find stock card
   * @param orderableIds orderable ids to find stock card
   * @return List of stock cards with SOH values, empty list if no stock cards were found.
   */
  public List<StockCard> getStockCardsWithStockOnHandByOrderableIds(
      UUID programId, UUID facilityId, List<UUID> orderableIds) {

    List<StockCard> stockCards = stockCardRepository.findByOrderableIdInAndProgramIdAndFacilityId(
        orderableIds, programId, facilityId);

    if (null == stockCards) {
      return Collections.emptyList();
    }

    stockCards.forEach(stockCard ->
        fetchStockOnHand(stockCard, LocalDate.now()));

    return stockCards;
  }

  /**
   * Fetch stock on hand value for given stock card and current date.
   *
   * @param stockCard stock card where the value will be set
   */
  public void fetchCurrentStockOnHand(StockCard stockCard) {
    fetchStockOnHandForSpecificDate(stockCard, LocalDate.now());
  }

  /**
   * Fetch stock on hand value for given stock card.
   *
   * @param stockCard stock card where the value will be set
   * @param asOfDate  date used to get latest stock on hand before or equal specific date. If date
   *                  is not specified, current date will be used.
   */
  public void fetchStockOnHandForSpecificDate(StockCard stockCard, LocalDate asOfDate) {
    LocalDate queryDate = null == asOfDate ? LocalDate.now() : asOfDate;
    fetchStockOnHand(stockCard, queryDate);
  }

  /**
   * Recalculate values of stock on hand for the first line item from all different stock on hand,
   * which in result will update soh for all following line items from the list as well.
   *
   * @param lineItems line items to recalculate the value for.
   */
  @Transactional
  public void recalculateStockOnHand(List<StockCardLineItem> lineItems) {
    Map<StockCard, List<StockCardLineItem>> map = mapStockCardsWithLineItems(lineItems);
    map.forEach((key, value) -> {
      value.sort(StockCard.getLineItemsComparator());
      value.stream().findFirst()
          .ifPresent(item -> recalculateStockOnHand(item.getStockCard(), item));
    });
  }

  /**
   * Recalculate values of stock on hand in all line items that happened after one given in a
   * parameter.
   *
   * @param stockCard stock card for which the value will be calculated.
   * @param lineItem  first line item to consider in recalculation.
   */
  private void recalculateStockOnHand(StockCard stockCard, StockCardLineItem lineItem) {
    Profiler profiler = new Profiler("RECALCULATE_STOCK_ON_HAND");
    profiler.setLogger(LOGGER);

    profiler.start("GET_LINE_ITEMS_PREVIOUS_STOCK_ON_HAND");
    int lineItemsPreviousStockOnHand = calculatedStockOnHandRepository
        .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
            stockCard.getId(), lineItem.getOccurredDate().minusDays(1)).orElseGet(() -> {
              CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHand();
              calculatedStockOnHand.setStockOnHand(0);
              return calculatedStockOnHand;
            }).getStockOnHand();

    profiler.start("GET_FOLLOWING_CALCULATED_STOCK_ON_HANDS");
    List<CalculatedStockOnHand> followingStockOnHands = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(), lineItem.getOccurredDate());

    profiler.start("DELETE_FOLLOWING_CALCULATED_STOCK_ON_HANDS");
    calculatedStockOnHandRepository.delete(followingStockOnHands);

    profiler.start("GET_FOLLOWING_STOCK_CARD_LINE_ITEMS");
    List<StockCardLineItem> followingLineItems = stockCard.getLineItems()
        .stream()
        .filter(item -> !item.getOccurredDate().isBefore(lineItem.getOccurredDate())
            && item.getId() != lineItem.getId()).sorted(StockCard.getLineItemsComparator())
        .collect(Collectors.toList());

    followingLineItems.add(0, lineItem);
    profiler.start("SAVE_RECALCULATED_STOCK_ON_HANDS");
    for (StockCardLineItem item : followingLineItems) {
      Integer calculatedStockOnHand = calculateStockOnHand(item, lineItemsPreviousStockOnHand);
      saveCalculatedStockOnHand(item, calculatedStockOnHand, stockCard);
      lineItemsPreviousStockOnHand = calculatedStockOnHand;
    }
    profiler.stop().log();
  }

  private Map<StockCard, List<StockCardLineItem>> mapStockCardsWithLineItems(
      List<StockCardLineItem> lineItems) {
    Map<StockCard, List<StockCardLineItem>> map = new HashMap<>();
    for (StockCardLineItem item : lineItems) {
      if (!map.containsKey(item.getStockCard())) {
        map.put(item.getStockCard(), new ArrayList<>());
      }
      map.get(item.getStockCard()).add(item);
    }
    return map;
  }

  private void fetchStockOnHand(StockCard stockCard, LocalDate asOfDate) {
    Optional<CalculatedStockOnHand> calculatedStockOnHandOptional = calculatedStockOnHandRepository
        .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
            stockCard.getId(), asOfDate);

    if (calculatedStockOnHandOptional.isPresent()) {
      CalculatedStockOnHand calculatedStockOnHand = calculatedStockOnHandOptional.get();
      stockCard.setStockOnHand(calculatedStockOnHand.getStockOnHand());
      stockCard.setOccurredDate(calculatedStockOnHand.getOccurredDate());
      stockCard.setProcessedDate(calculatedStockOnHand.getProcessedDate());
    }
  }

  private void saveCalculatedStockOnHand(StockCardLineItem lineItem, Integer stockOnHand,
      StockCard stockCard) {
    Optional<CalculatedStockOnHand> stockOnHandOfExistingOccurredDate =
        calculatedStockOnHandRepository.findFirstByStockCardIdAndOccurredDate(
            stockCard.getId(), lineItem.getOccurredDate());

    stockOnHandOfExistingOccurredDate.ifPresent((soh -> {
      CalculatedStockOnHand existingStockOnHand = stockOnHandOfExistingOccurredDate.get();
      calculatedStockOnHandRepository.delete(existingStockOnHand);
    }));

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(stockOnHand, stockCard,
        lineItem.getOccurredDate(), lineItem.getProcessedDate()));
  }

  /**
   * Recalculate values of stock on hand for single line item and returns aggregated stock on hand.
   * It's designed to aggregate a collection of line items.
   *
   * @param item    containing quantity that will be aggregated with prevSoH
   * @param prevSoH tmp stock on hand calculated for previous line items
   * @return prevSoH aggregated with quantity of line item
   */
  private Integer calculateStockOnHand(StockCardLineItem item, int prevSoH) {
    int quantity = item.isPhysicalInventory()
        ? item.getQuantity()
        : prevSoH + item.getQuantityWithSign();

    if (quantity < 0) {
      throwQuantityExceedException(item, prevSoH);
    }

    return quantity;
  }

  private void throwQuantityExceedException(StockCardLineItem item, int prevSoH) {
    OrderableDto orderable = orderableService.findOne(item.getStockCard().getOrderableId());
    String code = (orderable != null) ? orderable.getProductCode() : "";
    throw new ValidationMessageException(
        new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH,
            item.getOccurredDate(), code, prevSoH, item.getQuantity()));
  }
}
