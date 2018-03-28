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

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.stereotype.Service;

@Service
public class SupervisoryNodeReferenceDataService
    extends BaseReferenceDataService<SupervisoryNodeDto> {

  static final String PROGRAM_ID = "programId";
  static final String FACILITY_ID = "facilityId";

  @Override
  protected String getUrl() {
    return "/api/supervisoryNodes/";
  }

  @Override
  protected Class<SupervisoryNodeDto> getResultClass() {
    return SupervisoryNodeDto.class;
  }

  @Override
  protected Class<SupervisoryNodeDto[]> getArrayResultClass() {
    return SupervisoryNodeDto[].class;
  }

  /**
   * Find a correct supervisory node by the provided facility and program.
   */
  public SupervisoryNodeDto findSupervisoryNode(UUID program, UUID facility) {
    List<SupervisoryNodeDto> content = getPage(RequestParameters.init()
        .set(PROGRAM_ID, program)
        .set(FACILITY_ID, facility))
        .getContent();
    return content.size() > 0 ? content.get(0) : null;
  }

}
