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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_ASSIGNMENT_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_ID_EMPTY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_NOT_FOUND;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.ValidReasonAssignmentDto;
import org.openlmis.stockmanagement.dto.builder.ValidReasonAssignmentDtoBuilder;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

  @Autowired
  private ValidReasonAssignmentDtoBuilder reasonAssignmentDtoBuilder;

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardsController.class);

  /**
   * Get a list of valid reasons.
   *
   * @param queryParams request parameters
   * @return A list of valid reason dto.
   */
  @RequestMapping(value = "/validReasons", method = GET)
  @ResponseBody
  public List<ValidReasonAssignmentDto> getValidReasons(
      @RequestParam MultiValueMap<String, Object> queryParams) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Try to find stock card line item reason");
    }
    ValidReasonAssignmentSearchParams params = new ValidReasonAssignmentSearchParams(queryParams);

    List<ValidReasonAssignment> reasons =  reasonAssignmentRepository.search(params.getProgram(),
        params.getFacilityType(),
        params.getReasonType(), params.getReason());

    return reasonAssignmentDtoBuilder.build(reasons);
  }

  /**
   * Remove a reason assignment.
   *
   * @param assignmentId reason assignment id.
   * @return No content status.
   */
  @RequestMapping(value = "/validReasons/{id}", method = DELETE)
  public ResponseEntity removeReasonAssignment(@PathVariable("id") UUID assignmentId) {
    LOGGER.debug("Try to remove reason assignment {}", assignmentId);

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
   * @param assignmentDto valid reason assignment.
   * @return the assigned reason and program and facility type.
   */
  @RequestMapping(value = "/validReasons", method = POST)
  public ResponseEntity<ValidReasonAssignmentDto> assignReason(
      @RequestBody ValidReasonAssignmentDto assignmentDto) {
    permissionService.canManageReasons();
    assignmentDto.setId(null);
    ValidReasonAssignment assignment = ValidReasonAssignment.newInstance(assignmentDto);
    checkIsValidRequest(assignment);
    return findExistingOrSaveNew(assignment);
  }

  private void checkIsValidRequest(ValidReasonAssignment assignment) {
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(
        assignment.getProgramId(), assignment.getFacilityTypeId());
    if (assignment.getReason() == null || assignment.getReason().getId() == null) {
      throw new ValidationMessageException(new Message(ERROR_REASON_ID_EMPTY));
    }

    if (!reasonRepository.exists(assignment.getReason().getId())) {
      throw new ValidationMessageException(new Message(ERROR_REASON_NOT_FOUND));
    }
  }

  private ResponseEntity<ValidReasonAssignmentDto> findExistingOrSaveNew(
      ValidReasonAssignment assignment) {
    UUID programId = assignment.getProgramId();
    UUID facilityTypeId = assignment.getFacilityTypeId();
    UUID reasonId = assignment.getReason().getId();

    ValidReasonAssignment foundAssignment = reasonAssignmentRepository
        .findByProgramIdAndFacilityTypeIdAndReasonId(programId, facilityTypeId, reasonId);

    if (foundAssignment != null) {
      return new ResponseEntity<>(reasonAssignmentDtoBuilder.build(foundAssignment), OK);
    }

    ValidReasonAssignmentDto assignmentDto =
        reasonAssignmentDtoBuilder.build(reasonAssignmentRepository.save(assignment));
    return new ResponseEntity<>(assignmentDto, CREATED);
  }
}
