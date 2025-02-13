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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ProgramReferenceDataService extends BaseReferenceDataService<ProgramDto> {

  @Override
  protected String getUrl() {
    return "/api/programs/";
  }

  @Override
  protected Class<ProgramDto> getResultClass() {
    return ProgramDto.class;
  }

  @Override
  protected Class<ProgramDto[]> getArrayResultClass() {
    return ProgramDto[].class;
  }

  /**
   * Find program by unique code.
   *
   * @param programCode the program code, not null
   * @return an optional with found program, never null
   */
  public Optional<ProgramDto> findByCode(String programCode) {
    if (StringUtils.isBlank(programCode)) {
      return Optional.empty();
    }

    final RequestParameters parameters = RequestParameters.init().set("code", programCode);

    final Collection<ProgramDto> programDtos = findAll("", parameters);
    return programDtos.stream().findFirst();
  }

  /**
   * Find Program by IDs.
   *
   * @param ids the ids, not null
   * @return the list of lots, never null
   */
  public List<ProgramDto> findByIds(Collection<UUID> ids) {
    return CollectionUtils.isEmpty(ids) ? Collections.emptyList() :
        new ArrayList<>(findAll("", RequestParameters.init().set("id", ids)));
  }
}
