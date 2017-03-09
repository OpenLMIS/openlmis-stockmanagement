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

package org.openlmis.stockmanagement.web;

import static java.lang.String.format;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_ASSIGNMENT_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_ID_EMPTY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_NOT_FOUND;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.domain.adjustment.ValidReasonAssignment;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class ValidReasonAssignmentController {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private ValidReasonAssignmentRepository reasonAssignmentRepository;

  @Autowired
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Autowired
  private PermissionService permissionService;

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardsController.class);

  /**
   * Get a list of valid reasons.
   *
   * @param program      program id.
   * @param facilityType facility type id.
   * @return A list of valid reason.
   */
  @RequestMapping(value = "/validReasons", method = GET)
  public ResponseEntity<List<ValidReasonAssignment>> getValidReasons(
      @RequestParam("program") UUID program, @RequestParam("facilityType") UUID facilityType) {
    LOGGER.debug(format(
        "Try to find stock card line item reason with program %s and facility type %s",
        program.toString(), facilityType.toString()));
    permissionService.canViewReasons(program, facilityType);
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(program, facilityType);
    return new ResponseEntity<>(getReasonsBy(program, facilityType), OK);
  }

  /**
   * Remove a reason assignment.
   *
   * @param assignmentId reason assignment id.
   * @return No content status.
   */
  @RequestMapping(value = "/validReasons/{id}", method = DELETE)
  public ResponseEntity removeReasonAssignment(@PathVariable("id") UUID assignmentId) {
    LOGGER.debug(format("Try to remove reason assignment %s.", assignmentId));

    permissionService.canManageReasons();

    if (!reasonAssignmentRepository.exists(assignmentId)) {
      throw new ValidationMessageException(new Message(ERROR_REASON_ASSIGNMENT_NOT_FOUND));
    }
    reasonAssignmentRepository.delete(assignmentId);

    return new ResponseEntity<>(null, NO_CONTENT);
  }

  /**
   * Assign a reason to program and facility type.
   * If valid reason assignment ID is specified, ID will be ignored.
   *
   * @param assignment valid reason assignment.
   * @return the assigned reason and program and facility type.
   */
  @RequestMapping(value = "/validReasons", method = POST)
  public ResponseEntity<ValidReasonAssignment> assignReason(
      @RequestBody ValidReasonAssignment assignment) {
    permissionService.canManageReasons();
    assignment.setId(null);
    checkIsValidRequest(assignment);
    return findExistingOrSaveNew(assignment);
  }

  private void checkIsValidRequest(ValidReasonAssignment assignment) {
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(
        assignment.getProgramId(), assignment.getFacilityTypeId());

    permissionService.canManageReasons();

    if (assignment.getReason() == null || assignment.getReason().getId() == null) {
      throw new ValidationMessageException(new Message(ERROR_REASON_ID_EMPTY));
    }

    if (!reasonRepository.exists(assignment.getReason().getId())) {
      throw new ValidationMessageException(new Message(ERROR_REASON_NOT_FOUND));
    }
  }

  private ResponseEntity<ValidReasonAssignment> findExistingOrSaveNew(
      ValidReasonAssignment assignment) {
    UUID programId = assignment.getProgramId();
    UUID facilityTypeId = assignment.getFacilityTypeId();
    UUID reasonId = assignment.getReason().getId();

    ValidReasonAssignment foundAssignment = reasonAssignmentRepository
        .findByProgramIdAndFacilityTypeIdAndReasonId(programId, facilityTypeId, reasonId);

    if (foundAssignment != null) {
      return new ResponseEntity<>(foundAssignment, OK);
    }

    return new ResponseEntity<>(reasonAssignmentRepository.save(assignment), CREATED);
  }

  private List<ValidReasonAssignment> getReasonsBy(UUID program, UUID facilityType) {
    return reasonAssignmentRepository
        .findByProgramIdAndFacilityTypeId(program, facilityType);
  }

}
