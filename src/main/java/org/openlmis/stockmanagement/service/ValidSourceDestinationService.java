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

import static java.util.Collections.emptyList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_TYPE_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;

import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.ValidDestinationAssignmentDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityTypeReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidSourceDestinationService {

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private FacilityTypeReferenceDataService facilityTypeReferenceDataService;

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationRepository;

  /**
   * Find valid destinations by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid destination assignment DTOs
   */
  public List<ValidDestinationAssignmentDto> findValidDestinations(
      UUID programId, UUID facilityTypeId) {
    checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    List<ValidDestinationAssignment> destinationAssignments =
        validDestinationRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    if (destinationAssignments.isEmpty()) {
      return emptyList();
    }

    return destinationAssignments.stream()
        .map(destinations -> destinationToDto()).collect(Collectors.toList());
  }

  private ValidDestinationAssignmentDto destinationToDto() {
    return null;
  }

  private void checkProgramAndFacilityTypeExist(UUID programId, UUID facilityTypeId) {
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
}
