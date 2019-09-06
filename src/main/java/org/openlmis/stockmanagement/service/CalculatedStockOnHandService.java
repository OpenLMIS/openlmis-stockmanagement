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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculatedStockOnHandService {

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

    stockCards.forEach(stockCard -> fetchStockOnHand(stockCard, asOfDate));

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
    return getStockCardsWithStockOnHand(programId, facilityId, LocalDate.now());
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
  public void recalculateStockOnHand(StockCard stockCard, StockCardLineItem lineItem) {
    List<CalculatedStockOnHand> stockOnHands = calculatedStockOnHandRepository
            .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
                    stockCard.getId(), lineItem.getOccurredDate());

    if (stockOnHands.size() == 0
        || !stockOnHands.get(0).getOccurredDate().equals(lineItem.getOccurredDate())) {
      Integer previousStockOnHand = calculatedStockOnHandRepository
          .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
              stockCard.getId(), lineItem.getOccurredDate().minusDays(1)).orElseGet(() -> {
                CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHand();
                calculatedStockOnHand.setStockOnHand(0);
                return calculatedStockOnHand;
              }).getStockOnHand();

      calculatedStockOnHandRepository.save(new CalculatedStockOnHand(
          lineItem.getQuantity() + previousStockOnHand,
          stockCard, lineItem.getOccurredDate(),
          lineItem.getProcessedDate()));
    }

    stockOnHands.forEach(stockOnHand ->
        stockOnHand.setStockOnHand(stockOnHand.getStockOnHand() + lineItem.getQuantity()));

    calculatedStockOnHandRepository.save(stockOnHands);
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
}
