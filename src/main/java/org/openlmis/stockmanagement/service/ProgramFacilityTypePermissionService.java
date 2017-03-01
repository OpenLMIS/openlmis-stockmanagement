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

import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.SupportedProgramDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProgramFacilityTypePermissionService {
  @Autowired
  private AuthenticationHelper authenticationHelper;

  /**
   * Check program and home facility permission.
   * @param programId program id.
   * @param facilityTypeId facility type id.
   */
  public void checkProgramFacility(UUID programId, UUID facilityTypeId) {
    FacilityDto homeFacility = authenticationHelper.getCurrentUser().getHomeFacility();

    if (homeFacility == null || !facilityTypeId.equals(homeFacility.getType().getId())) {
      throwException(ERROR_FACILITY_TYPE_HOME_FACILITY_TYPE_NOT_MATCH, facilityTypeId.toString());
    }

    List<SupportedProgramDto> supportedPrograms = homeFacility.getSupportedPrograms();
    boolean isSupported = supportedPrograms.stream()
        .anyMatch(supportedProgram -> programId.equals(supportedProgram.getId()));
    if (!isSupported) {
      throwException(ERROR_PROGRAM_NOT_SUPPORTED, programId.toString());
    }
  }

  private void throwException(String errorKey, String... params) {
    throw new PermissionMessageException(new Message(errorKey, params));
  }
}
