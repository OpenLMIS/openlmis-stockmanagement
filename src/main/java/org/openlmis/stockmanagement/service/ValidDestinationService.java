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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_ASSIGNMENT_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_FOUND;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidDestinationService extends SourceDestinationBaseService {

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationRepository;

  /**
   * Find valid sources by program ID and facility type ID.
   *
   * @param programId program ID
   * @param facilityId facility ID
   * @return valid source assignment DTOs
   */
  public List<ValidSourceDestinationDto> findDestinations(UUID programId, UUID facilityId) {
    return findAssignments(programId, facilityId, validDestinationRepository);
  }

  /**
   * Assign a destination to a program and facility type.
   *
   * @param assignment assignment JPA model
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto assignDestination(ValidDestinationAssignment assignment) {
    assignment.setId(null);
    return doAssign(assignment, ERROR_DESTINATION_NOT_FOUND, validDestinationRepository);
  }

  /**
   * Find existing destination assignment.
   *
   * @param assignment assignment JPA model
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto findByProgramFacilityDestination(
      ValidDestinationAssignment assignment) {
    return findAssignment(assignment, validDestinationRepository);
  }

  /**
   * Find existing destinations.
   *
   * @param assignmentId destination assignment Id
   * @return assigmnet dto
   * @throws ValidationMessageException when assignment was not found
   */
  public ValidSourceDestinationDto findById(UUID assignmentId) {
    return findById(assignmentId, validDestinationRepository,
        ERROR_DESTINATION_ASSIGNMENT_NOT_FOUND);
  }

  /**
   * Delete a destination assignment by Id.
   *
   * @param assignmentId destination assignment Id
   */
  public void deleteDestinationAssignmentById(UUID assignmentId) {
    doDelete(assignmentId, validDestinationRepository, ERROR_DESTINATION_ASSIGNMENT_NOT_FOUND);
  }

}
