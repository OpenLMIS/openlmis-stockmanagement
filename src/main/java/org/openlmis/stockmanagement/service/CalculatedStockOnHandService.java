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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
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

  /**
   * Returns list of stock cards with fetched Stock on Hand values.
   *
   * @param programId program id to find stock cards
   * @param facilityId facility id to find stock cards
   * @param asOfDate date used to get latest stock on hand before or equal specific date
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
   * @param programId program id to find stock cards
   * @param facilityId facility id to find stock cards
   * @return List of stock cards with SOH values, empty list if no stock cards were found.
   */
  public List<StockCard> getStockCardsWithStockOnHand(UUID programId, UUID facilityId) {
    return getStockCardsWithStockOnHand(programId, facilityId, null);
  }

  /**
   * Returns list of stock cards with fetched Stock on Hand values.
   *
   * @param programId program id to find stock card
   * @param facilityId facility id to find stock card
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
   * Fetch stock on hand value for given stock card.
   *
   * @param stockCard stock card where the value will be set
   * @param asOfDate date used to get latest stock on hand before or equal specific date. If date
   *     is not specified, current date will be used.
   */
  public void fetchStockOnHandForSpecificDate(StockCard stockCard, LocalDate asOfDate) {
    LocalDate queryDate = null == asOfDate ? LocalDate.now() : asOfDate;
    fetchStockOnHand(stockCard, queryDate);
  }

  /**
   * Recalculates values of stock on hand
   * in all line items that happened after one given in parameter.
   *
   * @param stockCard stock card for which
   * @param lineItem  date used to get latest stock on hand before or equal specific date. If date
   *     is not specified, current date will be used.
   */
  public void recalculateStockOnHand(
      StockCard stockCard, StockCardLineItem lineItem) {
    Profiler profiler = new Profiler("RECALCULATE_STOCK_ON_HAND");
    profiler.setLogger(LOGGER);

    profiler.start("GET_LINE_ITEMS_PREVIOUS_STOCK_ON_HAND");
    int lineItemsPreviousStockOnHand = calculatedStockOnHandRepository
        .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
            stockCard.getId(), lineItem.getOccurredDate()).orElseGet(() -> {
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
        .filter(item -> item.getOccurredDate().isAfter(lineItem.getOccurredDate()))
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

  private Integer calculateStockOnHand(StockCardLineItem lineItem, Integer previousStockOnHand) {
    int quantity = lineItem.isPhysicalInventory()
        ? lineItem.getQuantity()
        : previousStockOnHand + lineItem.getQuantityWithSign();

    if (quantity < 0) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, previousStockOnHand,
              lineItem.getQuantity()));
    }

    return quantity;
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
}
