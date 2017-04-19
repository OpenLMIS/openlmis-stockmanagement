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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
   * @param fullSupply whether the full supply or non-full supply products should be retrieved
   * @return a collection of approved products matching the search criteria
   */
  public Collection<ApprovedProductDto> getApprovedProducts(UUID facilityId, UUID programId,
                                                            boolean fullSupply) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("programId", programId);
    parameters.put("fullSupply", fullSupply);

    return findAll(facilityId + "/approvedProducts", parameters);
  }

  /**
   * Retrieves all facility approved products from the reference data service, based on the
   * provided facility. It can be optionally filtered by the program ID.
   *
   * @param facilityId id of the facility
   * @param programId  id of the program
   * @return a collection of approved products matching the search criteria.
   */
  public List<OrderableDto> getAllApprovedProducts(UUID programId, UUID facilityId) {
    Collection<ApprovedProductDto> fullSupply =
        getApprovedProducts(facilityId, programId, true);

    Collection<ApprovedProductDto> nonFullSupply =
        getApprovedProducts(facilityId, programId, false);

    return concat(fullSupply.stream(), nonFullSupply.stream())
        .map(approvedProduct -> approvedProduct.getOrderable())
        .collect(toList());
  }

}
