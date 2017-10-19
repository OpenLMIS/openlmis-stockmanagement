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

package org.openlmis.stockmanagement.service;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_TYPE_HOME_FACILITY_TYPE_NOT_MATCH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_SUPPORTED;

import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HomeFacilityPermissionService {
  @Autowired
  private AuthenticationHelper authenticationHelper;
  
  @Autowired
  private FacilityReferenceDataService facilityService;

  /**
   * 1 Check if program is supported by user's home facility.
   * 2 Check if facility type matches user's home facility's type.
   * Will throw exception if any of the above two fails.
   *
   * @param programId      program id.
   * @param facilityTypeId facility type id.
   */
  public void checkProgramAndFacilityType(UUID programId, UUID facilityTypeId) {
    checkProgramSupported(programId);
    checkFacilityTypeMatches(facilityTypeId);
  }

  /**
   * Check if program is supported by user's home facility.
   *
   * @param programId  the program's id.
   */
  public void checkProgramSupported(UUID programId) {
    UUID homeFacilityId = authenticationHelper.getCurrentUser().getHomeFacilityId();
    FacilityDto homeFacility = null;
    if (homeFacilityId != null) {
      homeFacility = facilityService.findOne(homeFacilityId);
    }

    boolean isSupported = homeFacility != null
        && homeFacility.getSupportedPrograms().stream()
        .anyMatch(supportedProgram -> programId.equals(supportedProgram.getId()));
    if (!isSupported) {
      throwException(ERROR_PROGRAM_NOT_SUPPORTED, programId.toString());
    }
  }

  private void checkFacilityTypeMatches(UUID facilityTypeId) {
    UUID homeFacilityId = authenticationHelper.getCurrentUser().getHomeFacilityId();
    FacilityDto homeFacility = null;
    if (homeFacilityId != null) {
      homeFacility = facilityService.findOne(homeFacilityId);
    }

    if (homeFacility == null || !facilityTypeId.equals(homeFacility.getType().getId())) {
      throwException(ERROR_FACILITY_TYPE_HOME_FACILITY_TYPE_NOT_MATCH, facilityTypeId.toString());
    }
  }

  private void throwException(String errorKey, String... params) {
    throw new PermissionMessageException(new Message(errorKey, (Object)params));
  }
}