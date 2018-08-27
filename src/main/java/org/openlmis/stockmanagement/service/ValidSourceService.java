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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_ASSIGNMENT_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_FOUND;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidSourceService extends SourceDestinationBaseService {

  @Autowired
  private ValidSourceAssignmentRepository validSourceRepository;

  /**
   * Find valid destinations by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid destination assignment DTOs
   */
  public List<ValidSourceDestinationDto> findSources(UUID programId, UUID facilityTypeId) {
    return findAssignments(programId, facilityTypeId, validSourceRepository);
  }

  /**
   * Assign a source to a program and facility type.
   *
   * @param assignment assignment JPA model
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto assignSource(ValidSourceAssignment assignment) {
    assignment.setId(null);
    return doAssign(assignment, ERROR_SOURCE_NOT_FOUND, validSourceRepository);
  }

  /**
   * Find existing source assignment.
   *
   * @param assignment assignment JPA model
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto findByProgramFacilitySource(
      ValidSourceAssignment assignment) {
    return findAssignment(assignment, validSourceRepository);
  }

  /**
   * Delete a source assignment by Id.
   *
   * @param assignmentId source assignment Id
   */
  public void deleteSourceAssignmentById(UUID assignmentId) {
    doDelete(assignmentId, validSourceRepository, ERROR_SOURCE_ASSIGNMENT_NOT_FOUND);
  }

}
