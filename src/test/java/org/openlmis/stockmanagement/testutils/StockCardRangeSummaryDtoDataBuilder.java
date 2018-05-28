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

package org.openlmis.stockmanagement.testutils;

import java.util.HashMap;
import java.util.Map;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.web.stockcardrangesummary.StockCardRangeSummaryDto;

public class StockCardRangeSummaryDtoDataBuilder {

  private ObjectReferenceDto orderable;
  private Long stockOutDays;
  private Map<String, Integer> tags;

  /**
   * Creates builder for creating new instance of {@link StockCardRangeSummaryDtoDataBuilder}.
   */
  public StockCardRangeSummaryDtoDataBuilder() {
    orderable = new ObjectReferenceDtoDataBuilder().withPath("api/orderables").build();
    stockOutDays = 0L;
    tags = new HashMap<>();
  }

  /**
   * Creates new instance of {@link StockCardRangeSummaryDto} with properties.
   * @return created stock cards range summary
   */
  public StockCardRangeSummaryDto build() {
    return new StockCardRangeSummaryDto(orderable, stockOutDays, tags);
  }

  public StockCardRangeSummaryDtoDataBuilder withOrderable(ObjectReferenceDto orderable) {
    this.orderable = orderable;
    return this;
  }

  public StockCardRangeSummaryDtoDataBuilder withStockOutDays(Long stockOutDays) {
    this.stockOutDays = stockOutDays;
    return this;
  }

  public StockCardRangeSummaryDtoDataBuilder withTags(Map<String, Integer> tags) {
    this.tags = tags;
    return this;
  }
}
