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

import org.openlmis.stockmanagement.dto.CanFulfillForMeEntryDto;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.web.stockcardsummariesv2.StockCardSummaryV2Dto;
import java.util.HashSet;
import java.util.Set;

public class StockCardSummaryV2DtoDataBuilder {

  private ObjectReferenceDto orderable;
  private Set<CanFulfillForMeEntryDto> canFulfillForMe;

  public StockCardSummaryV2DtoDataBuilder() {
    orderable = new ObjectReferenceDtoDataBuilder().withPath("api/orderables").build();
    canFulfillForMe = new HashSet<>();
  }

  /**
   * Creates new instance of {@link StockCardSummaryV2Dto} with properties.
   * @return created stock summary card V2
   */
  public StockCardSummaryV2Dto build() {
    return new StockCardSummaryV2Dto(orderable, canFulfillForMe);
  }

  public StockCardSummaryV2DtoDataBuilder withCanFulfillForMe(
      CanFulfillForMeEntryDto canFulfillForMe) {
    this.canFulfillForMe.add(canFulfillForMe);
    return this;
  }

  public StockCardSummaryV2DtoDataBuilder withCanFulfillForMe(
      Set<CanFulfillForMeEntryDto> canFulfillForMe) {
    this.canFulfillForMe = canFulfillForMe;
    return this;
  }
}
