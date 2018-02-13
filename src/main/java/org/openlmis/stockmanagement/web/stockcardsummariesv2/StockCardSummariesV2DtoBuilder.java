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

package org.openlmis.stockmanagement.web.stockcardsummariesv2;

import static org.apache.commons.collections.MapUtils.isEmpty;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.CanFulfillForMeEntryDto;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardSummaryV2Dto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StockCardSummariesV2DtoBuilder {

  private static final String API = "api/";

  @Value("${service.url}")
  private String serviceUrl;

  /**
   * Builds Stock Card Summary dtos from stock cards and orderables.
   *
   * @param approvedProducts list of {@link OrderableDto} that summaries will be based on
   * @param stockCards       list of {@link StockCard} found for orderables
   * @param orderables       map of orderable ids as keys and {@link OrderableFulfillDto}
   * @param asOfDate         date on which stock on hand will be retrieved
   * @return list of {@link StockCardSummaryV2Dto}
   */
  public List<StockCardSummaryV2Dto> build(List<OrderableDto> approvedProducts,
                                           List<StockCardDto> stockCards,
                                           Map<UUID, OrderableFulfillDto> orderables,
                                           LocalDate asOfDate) {
    return approvedProducts.stream()
        .map(p -> build(stockCards, p.getId(),
            isEmpty(orderables) ? null : orderables.get(p.getId()),
            asOfDate))
        .collect(Collectors.toList());
  }

  private StockCardSummaryV2Dto build(List<StockCardDto> stockCards, UUID orderableId,
                                      OrderableFulfillDto fulfills, LocalDate asOfDate) {
    return new StockCardSummaryV2Dto(
        createOrderableReference(orderableId),
        null == fulfills ? null : fulfills.getCanFulfillForMe()
            .stream()
            .map(id -> buildFulfillsEntry(id,
                findStockCardByOrderableId(id, stockCards),
                asOfDate))
            .collect(Collectors.toList()));
  }

  private CanFulfillForMeEntryDto buildFulfillsEntry(UUID orderableId, StockCardDto stockCard,
                                                     LocalDate asOfDate) {
    if (stockCard == null) {
      return new CanFulfillForMeEntryDto(createOrderableReference(orderableId));
    } else {
      StockCardLineItem lineItem;
      if (asOfDate == null) {
        lineItem = stockCard.getLineItemAsOfDate(LocalDate.now());
      } else {
        lineItem = stockCard.getLineItemAsOfDate(asOfDate);
      }

      return new CanFulfillForMeEntryDto(
          createStockCardReference(stockCard.getId()),
          createOrderableReference(orderableId),
          stockCard.getLotId() == null ? null : createLotReference(stockCard.getLotId()),
          lineItem == null ? null : lineItem.getStockOnHand(),
          lineItem == null ? null : lineItem.getProcessedDate()
      );
    }
  }

  private StockCardDto findStockCardByOrderableId(UUID orderableId, List<StockCardDto> stockCards) {
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
