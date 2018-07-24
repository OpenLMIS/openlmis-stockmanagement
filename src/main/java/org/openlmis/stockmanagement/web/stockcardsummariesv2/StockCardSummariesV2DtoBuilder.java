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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.MapUtils;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StockCardSummariesV2DtoBuilder {

  static final String ORDERABLES = "orderables";
  static final String STOCK_CARDS = "stockCards";
  static final String LOTS = "lots";

  @Value("${service.url}")
  private String serviceUrl;

  private boolean nonEmptySummariesOnly;

  public StockCardSummariesV2DtoBuilder nonEmptySummariesOnly() {
    nonEmptySummariesOnly = true;
    return this;
  }

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
                                           List<StockCard> stockCards,
                                           Map<UUID, OrderableFulfillDto> orderables,
                                           LocalDate asOfDate) {
    Stream<StockCardSummaryV2Dto> summariesStream = approvedProducts.stream()
        .map(p -> build(stockCards, p.getId(),
            MapUtils.isEmpty(orderables) ? null : orderables.get(p.getId()),
            asOfDate))
        .sorted();

    if (nonEmptySummariesOnly) {
      summariesStream = summariesStream.filter(summary -> !summary.getCanFulfillForMe().isEmpty());
    }

    return summariesStream.collect(toList());
  }

  private StockCardSummaryV2Dto build(List<StockCard> stockCards, UUID orderableId,
                                      OrderableFulfillDto fulfills, LocalDate asOfDate) {

    Set<CanFulfillForMeEntryDto> canFulfillSet = null == fulfills ? new HashSet<>()
        : fulfills.getCanFulfillForMe()
        .stream()
        .map(id -> buildFulfillsEntries(id,
            findStockCardByOrderableId(id, stockCards),
            asOfDate))
        .flatMap(List::stream)
        .collect(toSet());

    canFulfillSet.addAll(
        buildFulfillsEntries(
            orderableId,
            findStockCardByOrderableId(orderableId, stockCards),
            asOfDate));

    return new StockCardSummaryV2Dto(createOrderableReference(orderableId), canFulfillSet);
  }

  private List<CanFulfillForMeEntryDto> buildFulfillsEntries(UUID orderableId,
                                                     List<StockCard> stockCards,
                                                     LocalDate asOfDate) {
    if (isEmpty(stockCards)) {
      return Collections.emptyList();
    } else {
      return stockCards.stream().map(stockCard -> {
        StockCardLineItem lineItem;
        if (asOfDate == null) {
          lineItem = stockCard.getLineItemAsOfDate(LocalDate.now());
        } else {
          lineItem = stockCard.getLineItemAsOfDate(asOfDate);
        }

        return createCanFulfillForMeEntry(stockCard, orderableId, lineItem);
      }).collect(Collectors.toCollection(ArrayList::new));
    }
  }

  private CanFulfillForMeEntryDto createCanFulfillForMeEntry(StockCard stockCard, UUID orderableId,
                                                             StockCardLineItem lineItem) {
    if (null != lineItem) {
      return new CanFulfillForMeEntryDto(
          createStockCardReference(stockCard.getId()),
          createOrderableReference(orderableId),
          stockCard.getLotId() == null ? null : createLotReference(stockCard.getLotId()),
          lineItem.getStockOnHand(),
          lineItem.getProcessedDate(),
          lineItem.getOccurredDate()
      );
    }
    return new CanFulfillForMeEntryDto(
        createStockCardReference(stockCard.getId()),
        createOrderableReference(orderableId),
        stockCard.getLotId() == null ? null : createLotReference(stockCard.getLotId()),
        null,
        null,
        null
    );
  }

  private List<StockCard> findStockCardByOrderableId(UUID orderableId,
                                                        List<StockCard> stockCards) {
    return stockCards
        .stream()
        .filter(card -> card.getOrderableId().equals(orderableId))
        .collect(toList());
  }

  private ObjectReferenceDto createOrderableReference(UUID id) {
    return createReference(id, ORDERABLES);
  }

  private ObjectReferenceDto createStockCardReference(UUID id) {
    return createReference(id, STOCK_CARDS);
  }

  private ObjectReferenceDto createLotReference(UUID id) {
    return createReference(id, LOTS);
  }

  private ObjectReferenceDto createReference(UUID id, String resourceName) {
    return new ObjectReferenceDto(serviceUrl, resourceName, id);
  }
}
