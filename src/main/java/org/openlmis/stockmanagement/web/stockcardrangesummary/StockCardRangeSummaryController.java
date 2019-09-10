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

import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardAggregate;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stockCardRangeSummaries")
public class StockCardRangeSummaryController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(StockCardRangeSummaryController.class);

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  @Autowired
  private StockCardRangeSummaryBuilder builder;

  @Autowired
  private PermissionService permissionService;

  /**
   * Get stock card range summaries by program and facility.
   *
   * @return Stock card range summaries.
   */
  @GetMapping
  public Page<StockCardRangeSummaryDto> getStockCardRangeSummaries(
      @RequestParam MultiValueMap<String, String> parameters,
      @PageableDefault(size = Integer.MAX_VALUE) Pageable pageable) {

    Profiler profiler = new Profiler("GET_STOCK_CARD_RANGE_SUMMARIES");
    profiler.setLogger(LOGGER);

    profiler.start("VALIDATE_PARAMS");
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(parameters);

    profiler.start("PERMISSION_CHECK");
    permissionService.canViewStockCard(params.getProgramId(), params.getFacilityId());

    profiler.start("GET_STOCK_CARDS_SUMMARIES_SERVICE");
    Map<UUID, StockCardAggregate> groupedStockCards =
        stockCardSummariesService.getGroupedStockCards(
            params.getProgramId(),
            params.getFacilityId(),
            params.getOrderableIds(),
            params.getStartDate(),
            params.getEndDate());

    profiler.start("TO_DTO");
    Page<StockCardRangeSummaryDto> page = builder.build(
        groupedStockCards,
        params.getTag(),
        params.getStartDate(),
        params.getEndDate(),
        pageable);

    profiler.stop().log();
    return page;
  }
}
