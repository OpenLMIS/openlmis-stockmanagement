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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class FacilityReferenceDataService extends BaseReferenceDataService<FacilityDto> {

  @Override
  protected String getUrl() {
    return "/api/facilities/";
  }

  @Override
  protected Class<FacilityDto> getResultClass() {
    return FacilityDto.class;
  }

  @Override
  protected Class<FacilityDto[]> getArrayResultClass() {
    return FacilityDto[].class;
  }
  
  /**
   * Finds facilities by their ids.
   *
   * @param ids ids to look for.
   * @return map of ids and facilities
   */
  public Map<UUID, FacilityDto> findByIds(Collection<UUID> ids) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("id", ids);

    Page<FacilityDto> facilityDtos =  getPage(parameters);
    return facilityDtos.getContent().stream()
            .collect(Collectors.toMap(FacilityDto::getId, Function.identity()));
  }

  /**
   * Find facility by unique code.
   *
   * @param facilityCode the facility code, not null
   * @return an optional with found facility, never null
   */
  public Optional<FacilityDto> findByCode(String facilityCode) {
    final RequestParameters parameters = RequestParameters
        .init()
        .set("code", facilityCode);

    final Page<FacilityDto> facilityDtos =  getPage(parameters);
    return facilityDtos.stream().findFirst();
  }

  public boolean exists(UUID id) {
    return id != null && findOne(id) != null;
  }
}
