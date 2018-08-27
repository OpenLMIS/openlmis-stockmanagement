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

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams;

public class StockCardSummariesV2SearchParamsDataBuilder {

  private UUID programId;
  private UUID facilityId;
  private List<UUID> orderableId;
  private LocalDate asOfDate;
  private boolean nonEmptyOnly;

  /**
   * Creates builder for creating new instance of {@link StockCardSummariesV2SearchParams}.
   */
  public StockCardSummariesV2SearchParamsDataBuilder() {
    programId = UUID.randomUUID();
    facilityId = UUID.randomUUID();
    orderableId = asList(UUID.randomUUID(), UUID.randomUUID());
    asOfDate = LocalDate.now();
    nonEmptyOnly = false;
  }

  /**
   * Creates new instance of {@link StockCardSummariesV2SearchParams} with properties.
   * @return created stock card line item reason.
   */
  public StockCardSummariesV2SearchParams build() {
    return new StockCardSummariesV2SearchParams(programId, facilityId,
        orderableId, asOfDate, nonEmptyOnly);
  }

  public StockCardSummariesV2SearchParamsDataBuilder withoutFacilityId() {
    this.facilityId = null;
    return this;
  }

  public StockCardSummariesV2SearchParamsDataBuilder withoutProgramId() {
    this.programId = null;
    return this;
  }

  public StockCardSummariesV2SearchParamsDataBuilder withOrderableIds(List<UUID> orderableId) {
    this.orderableId = orderableId;
    return this;
  }
}
