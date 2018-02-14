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

import org.openlmis.stockmanagement.dto.StockCardSummaryV2Dto;
import org.openlmis.stockmanagement.service.StockCardSummaries;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams;
import org.openlmis.stockmanagement.web.StockCardsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/stockCardSummaries")
public class StockCardSummariesV2Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardsController.class);

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  @Autowired
  private StockCardSummariesV2DtoBuilder stockCardSummariesV2DtoBuilder;

  /**
   * Get stock card summaries by program and facility.
   *
   * @return Stock card summaries.
   */
  @GetMapping
  public Page<StockCardSummaryV2Dto> getStockCardSummaries(
      @RequestParam MultiValueMap<String, String> parameters) {

    Profiler profiler = new Profiler("GET_STOCK_CARDS_V2");
    profiler.setLogger(LOGGER);

    profiler.start("VALIDATE_PARAMS");
    StockCardSummariesV2SearchParams params = new StockCardSummariesV2SearchParams(parameters);
    params.validate();

    profiler.start("GET_STOCK_CARD_SUMMARIES");
    StockCardSummaries summaries = stockCardSummariesService.findStockCards(params);

    PageImpl<StockCardSummaryV2Dto> page =
        new PageImpl<>(
            stockCardSummariesV2DtoBuilder.build(summaries.getPageOfApprovedProducts(),
                summaries.getStockCardsForFulfillOrderables(), summaries.getOrderableFulfillMap(),
                summaries.getAsOfDate()),
            params.getPageable(), summaries.getTotalElements());

    profiler.stop().log();
    return page;
  }
}
