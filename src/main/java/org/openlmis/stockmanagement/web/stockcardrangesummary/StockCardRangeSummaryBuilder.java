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

package org.openlmis.stockmanagement.web.stockcardrangesummary;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.service.StockCardAggregate;
import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class StockCardRangeSummaryBuilder {

  static final String ORDERABLES = "orderables";

  @Value("${service.url}")
  private String serviceUrl;

  /**
   * Builds Stock Card Range Summary dtos from stock card aggregates.
   *
   * @param groupedStockCards map of orderable id and associated stock cards
   * @param tag               tag for filtering stock card line items
   * @param startDate         start date for filtering line items
   * @param endDate           end date for filtering line items
   * @param pageable          pagination parameters
   * @return page of {@link StockCardRangeSummaryDto}
   */
  public Page<StockCardRangeSummaryDto> build(
      Map<UUID, StockCardAggregate> groupedStockCards,
      String tag,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {

    Page<UUID> orderableIdPage = Pagination.getPage(
        groupedStockCards.keySet().stream()
            .sorted()
            .collect(toList()), pageable);

    List<StockCardRangeSummaryDto> pagedSummaries = orderableIdPage.getContent().stream()
        .map(orderableId -> createDto(
            groupedStockCards.get(orderableId), orderableId,
            startDate, endDate, tag))
        .collect(toList());

    return new PageImpl<>(pagedSummaries, pageable, groupedStockCards.keySet().size());
  }

  private StockCardRangeSummaryDto createDto(StockCardAggregate aggregate, UUID orderableId,
      LocalDate startDate, LocalDate endDate, String tag) {
    return new StockCardRangeSummaryDto(
        new ObjectReferenceDto(serviceUrl, ORDERABLES, orderableId),
        aggregate.getStockoutDays(startDate, endDate),
        null != tag
            ? ImmutableMap.of(tag, aggregate.getAmount(tag, startDate, endDate))
            : aggregate.getAmounts(startDate, endDate));
  }
}
