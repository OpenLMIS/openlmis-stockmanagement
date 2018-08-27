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

  private void throwException(String errorKey, String... params) {
    throw new PermissionMessageException(new Message(errorKey, (Object)params));
  }
}