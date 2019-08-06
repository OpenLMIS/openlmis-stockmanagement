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
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
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

  CalculatedStockOnHand saveFromStockCard(StockCard stockCard) {
    stockCard.calculateStockOnHand();
    return new CalculatedStockOnHand(stockCard.getStockOnHand(), stockCard, LocalDate.now());
  }

  /**
   * Returns list of stock cards with fetched Stock on Hand values.
   *
   * @param programId program id to find stock cards
   * @param facilityId faciliy id to find stock cards
   * @param asOfDate date used to get latest stock on hand before or equal specific date If date
   *     is not specified, current date will be used.
   * @return List of stock cards with SOH values, empty list if no stock cards were found.
   */
  public List<StockCard> getStockCardsWithStockOnHand(UUID programId,
      UUID facilityId, LocalDate asOfDate) {

    LocalDate queryDate = null == asOfDate ? LocalDate.now() : asOfDate;

    List<StockCard> stockCards = stockCardRepository
        .findByProgramIdAndFacilityId(programId, facilityId);

    if (null == stockCards) {
      return Collections.emptyList();
    }

    stockCards.forEach(stockCard -> fetchStockOnHand(stockCard, queryDate));

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

  private void fetchStockOnHand(StockCard stockCard, LocalDate asOfDate) {
    CalculatedStockOnHand calculatedStockOnHand = calculatedStockOnHandRepository
        .findFirstByStockCardIdAndDateBeforeOrderByDateDesc(stockCard.getId(), asOfDate);

    if (null != calculatedStockOnHand) {
      stockCard.setStockOnHand(calculatedStockOnHand.getStockOnHand());
      stockCard.setOccurredDate(calculatedStockOnHand.getDate());
    }
  }

}
