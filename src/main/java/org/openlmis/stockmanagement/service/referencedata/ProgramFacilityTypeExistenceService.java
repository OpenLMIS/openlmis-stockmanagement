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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_TYPE_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProgramFacilityTypeExistenceService {

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private FacilityTypeReferenceDataService facilityTypeReferenceDataService;

  /**
   * Check program and facility type existence.
   *
   * @param programId program id.
   * @param facilityTypeId facility type id.
   */
  public void checkProgramAndFacilityTypeExist(UUID programId, UUID facilityTypeId) {
    ProgramDto programDto = programReferenceDataService.findOne(programId);
    FacilityTypeDto facilityTypeDto = facilityTypeReferenceDataService.findOne(facilityTypeId);
    if (programDto == null) {
      throw new ValidationMessageException(
          new Message(ERROR_PROGRAM_NOT_FOUND, programId.toString()));
    }
    if (facilityTypeDto == null) {
      throw new ValidationMessageException(
          new Message(ERROR_FACILITY_TYPE_NOT_FOUND, facilityTypeId.toString()));
    }
  }

  /**
   * Check programs and facility type existence.
   *
   * @param programIds program ids.
   * @param facilityTypeId facility type id.
   */
  public void checkProgramsAndFacilityTypeExist(List<UUID> programIds, UUID facilityTypeId) {
    for (UUID programId : programIds) {
      checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    }
  }
}
