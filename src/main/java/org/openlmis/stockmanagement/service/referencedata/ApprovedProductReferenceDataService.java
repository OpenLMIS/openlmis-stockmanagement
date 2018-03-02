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

import org.openlmis.stockmanagement.dto.referencedata.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

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
   *
   * @param facilityId id of the facility
   * @param programId  id of the program
   * @param pageable   pagination parameters
   * @return a collection of approved products matching the search criteria
   */
  public Page<OrderableDto> getApprovedProducts(UUID facilityId, UUID programId,
                                                Collection<UUID> orderableIds, Pageable pageable) {
    RequestParameters params = RequestParameters.init();

    params.set("programId", programId);

    if (null != pageable) {
      params.set("size", pageable.getPageSize());
      params.set("page", pageable.getPageNumber());
    }

    if (!isEmpty(orderableIds)) {
      params.set("orderableId", orderableIds);
    }

    Page<ApprovedProductDto> approvedProductPage =
        getPage(facilityId + "/approvedProducts", params);

    return new PageImpl<>(approvedProductPage.getContent().stream()
        .map(ApprovedProductDto::getOrderable).collect(Collectors.toList()),
        pageable, approvedProductPage.getTotalElements());
  }
}
