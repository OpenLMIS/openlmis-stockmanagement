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

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.CanFulfillForMeEntryDto;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.dto.StockCardSummaryV2Dto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StockCardSummariesV2Builder {

  private static final String API = "api/";

  @Value("${service.url}")
  private String serviceUrl;

  /**
   * Builds Stock Card Summary dtos from stock cards and orderables.
   *
   * @param stockCards list of {@link StockCard} found for orderables
   * @param orderables map of orderable ids as keys and {@link OrderableFulfillDto}
   * @param asOfDate   date on which stock on hand will be retrieved from stock card line items
   * @return list of {@link StockCardSummaryV2Dto}
   */
  public List<StockCardSummaryV2Dto> build(List<StockCard> stockCards,
                                           Map<UUID, OrderableFulfillDto> orderables,
                                           LocalDate asOfDate) {
    return orderables.entrySet()
        .stream()
        .map(e -> build(stockCards, e.getKey(), e.getValue(), asOfDate))
        .collect(Collectors.toList());
  }

  private StockCardSummaryV2Dto build(List<StockCard> stockCards, UUID orderableId,
                                     OrderableFulfillDto fulfills, LocalDate asOfDate) {
    return new StockCardSummaryV2Dto(
        createOrderableReference(orderableId),
        fulfills.getCanFulfillForMe()
            .stream()
            .map(id -> buildFulfillsEntry(orderableId,
                findStockCardByOrderableId(id, stockCards),
                asOfDate))
            .collect(Collectors.toList()));
  }

  private CanFulfillForMeEntryDto buildFulfillsEntry(UUID orderableId, StockCard stockCard,
                                                     LocalDate asOfDate) {
    if (stockCard == null) {
      return new CanFulfillForMeEntryDto(createOrderableReference(orderableId));
    } else {
      StockCardLineItem lineItem;
      if (asOfDate == null) {
        lineItem = stockCard.getLineItemAsOfDate(ZonedDateTime.now());
      } else {
        lineItem = stockCard.getLineItemAsOfDate(asOfDate.atTime(LocalTime.MAX)
            .atZone(ZoneOffset.UTC));
      }

      return new CanFulfillForMeEntryDto(
          createStockCardReference(stockCard.getId()),
          createOrderableReference(orderableId),
          createLotReference(stockCard.getLotId()),
          lineItem == null ? null : lineItem.getStockOnHand(),
          lineItem == null ? null : lineItem.getProcessedDate()
      );
    }
  }

  private StockCard findStockCardByOrderableId(UUID orderableId, List<StockCard> stockCards) {
    return stockCards
        .stream()
        .filter(card -> card.getOrderableId().equals(orderableId))
        .findFirst()
        .orElse(null);
  }

  private ObjectReferenceDto createOrderableReference(UUID id) {
    return createReference(id, "orderables");
  }

  private ObjectReferenceDto createStockCardReference(UUID id) {
    return createReference(id, "stockCards");
  }

  private ObjectReferenceDto createLotReference(UUID id) {
    return createReference(id, "lots");
  }

  private ObjectReferenceDto createReference(UUID id, String resourceName) {
    return new ObjectReferenceDto(serviceUrl, API + resourceName, id);
  }
}
