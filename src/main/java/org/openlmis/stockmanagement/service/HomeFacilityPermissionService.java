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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_SUPPORTED;

import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomeFacilityPermissionService {

  static final String WS_TYPE_CODE = "WS";

  @Autowired
  private AuthenticationHelper authenticationHelper;
  
  @Autowired
  private FacilityReferenceDataService facilityService;

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

  /**
   * Returns true if facility is within the same geographic zone as the home facility.
   * Returns false otherwise or in case facility id is equal to home facility id.
   *
   * @param facilityId UUID of facility
   * @return boolean flag indicating linkage between facility and home facility
   */
  public boolean checkFacilityAndHomeFacilityLinkage(UUID facilityId) {
    UUID homeFacilityId = authenticationHelper.getCurrentUser().getHomeFacilityId();
    if (facilityId.equals(homeFacilityId)) {
      return false;
    }
    FacilityDto facility = facilityService.findOne(facilityId);
    if (facility.getType().getCode().equals(WS_TYPE_CODE)) {
      FacilityDto homeFacility = facilityService.findOne(homeFacilityId);
      return homeFacility.getGeographicZone().getId().equals(facility.getGeographicZone().getId());
    } else {
      return false;
    }
  }

  private void throwException(String errorKey, String... params) {
    throw new PermissionMessageException(new Message(errorKey, (Object)params));
  }
}
