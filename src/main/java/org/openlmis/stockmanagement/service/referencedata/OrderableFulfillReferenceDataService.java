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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.stereotype.Service;

@Service
public class OrderableFulfillReferenceDataService extends BaseReferenceDataService<Map> {

  @Override
  protected String getUrl() {
    return "/api/orderableFulfills/";
  }

  @Override
  protected Class<Map> getResultClass() {
    return Map.class;
  }

  @Override
  protected Class<Map[]> getArrayResultClass() {
    return Map[].class;
  }

  /**
   * Finds orderables by their ids.
   *
   * @param ids ids to look for.
   * @return a page of orderables
   */
  public Map<UUID, OrderableFulfillDto> findByIds(Collection<UUID> ids) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("id", ids);

    return getMap(null, parameters, UUID.class, OrderableFulfillDto.class);
  }

    /**
     * Finds orderables by facilityId and programId.
     *
     * @param facilityId id of facility which is used during searching orderables
     * @param programId id of program which is used during searching orderables
     * @return a page of orderables
     */
    public Map<UUID, OrderableFulfillDto> findByFacilityIdProgramId(UUID facilityId, UUID programId) {
      RequestParameters parameters = RequestParameters
          .init()
          .set("facilityId", facilityId)
          .set("programId", programId);

    return getMap(null, parameters, UUID.class, OrderableFulfillDto.class);
  }
}
