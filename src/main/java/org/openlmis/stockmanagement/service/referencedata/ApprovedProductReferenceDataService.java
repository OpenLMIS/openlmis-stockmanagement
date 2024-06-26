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

package org.openlmis.stockmanagement.service.referencedata;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.openlmis.stockmanagement.dto.referencedata.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderablesAggregator;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ApprovedProductReferenceDataService extends
    BaseReferenceDataService<ApprovedProductDto> {
  @Override
  protected String getUrl() {
    return "/api/facilities/";
  }

  @Override
  protected Class<ApprovedProductDto> getResultClass() {
    return ApprovedProductDto.class;
  }

  @Override
  protected Class<ApprovedProductDto[]> getArrayResultClass() {
    return ApprovedProductDto[].class;
  }

  /**
   * Retrieves all facility approved products from the reference data service, based on the
   * provided facility and full supply flag. It can be optionally filtered by the program ID.
   * The result is wrapped to a separate class to improve the performance
   *
   * @param facilityId id of the facility
   * @param programId  id of the program
   * @return wrapped collection of approved products matching the search criteria
   */
  public OrderablesAggregator getApprovedProducts(UUID facilityId, UUID programId,
                                                  Collection<UUID> orderableIds) {
    return this.getApprovedProducts(facilityId, Collections.singleton(programId), orderableIds,
        null, null);
  }

  /**
   * Retrieves all facility approved products from the reference data service, based on the
   * provided facility and full supply flag. It can be optionally filtered by the program ID.
   * The result is wrapped to a separate class to improve the performance
   *
   * @param facilityId id of the facility
   * @param programIds  ids of the programs
   * @param orderableIds Id of orderables
   * @param orderableCode Code of the orderables
   * @param orderableName Name of the orderables
   *
   * @return wrapped collection of approved products matching the search criteria
   */
  public OrderablesAggregator getApprovedProducts(
      UUID facilityId,
      Collection<UUID> programIds,
      Collection<UUID> orderableIds,
      String orderableCode,
      String orderableName
  ) {
    RequestParameters params = RequestParameters.init();

    programIds.forEach(id -> params.set("programId", id));

    if (!isEmpty(orderableIds)) {
      params.set("orderableId", orderableIds);
    }

    if (orderableCode != null) {
      params.set("orderableCode", orderableCode);
    }

    if (orderableName != null) {
      params.set("orderableName", orderableName);
    }

    Page<ApprovedProductDto> approvedProductPage =
        getPage(facilityId + "/approvedProducts", params);

    return new OrderablesAggregator(new ArrayList<>(approvedProductPage.getContent()));
  }
}
