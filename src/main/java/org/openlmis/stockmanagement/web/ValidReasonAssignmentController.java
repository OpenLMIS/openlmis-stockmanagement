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
import static java.util.stream.Collectors.toList;
import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.adjustment.ValidReasonAssignment;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class ValidReasonAssignmentController {
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
  @RequestMapping(value = "/validReasons")
  public ResponseEntity<List<StockCardLineItemReason>> getValidReasons(
      @RequestParam("program") UUID program, @RequestParam("facilityType") UUID facilityType) {
    LOGGER.debug(
        format("Try to find stock card line item reason with program %s and facility type %s",
            program.toString(), facilityType.toString()));

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(program, facilityType);
    permissionService.canViewReasons(program, facilityType);

    return new ResponseEntity<>(getReasons(program, facilityType), OK);
  }

  /**
   * Assign a reason to program and facility type.
   *
   * @param program      program id
   * @param facilityType facility type id
   * @param reasonId     reason id
   * @return the assigned reason and program and facility type.
   * @throws InstantiationException InstantiationException
   * @throws IllegalAccessException IllegalAccessException
   */
  @RequestMapping(value = "/validReasons", method = POST)
  public ResponseEntity<ValidReasonAssignment> assignReason(
      @RequestParam("program") UUID program,
      @RequestParam("facilityType") UUID facilityType,
      @RequestBody UUID reasonId) throws InstantiationException, IllegalAccessException {

    ValidReasonAssignment assignment = reasonAssignmentRepository
        .findByProgramIdAndFacilityTypeIdAndReasonId(program, facilityType, reasonId);

    if (assignment != null) {
      return new ResponseEntity<>(assignment, OK);
    }

    return new ResponseEntity<>(saveAssignment(program, facilityType, reasonId), CREATED);
  }

  private ValidReasonAssignment saveAssignment(UUID program, UUID facilityType, UUID reasonId)
      throws IllegalAccessException, InstantiationException {
    return reasonAssignmentRepository.save(new ValidReasonAssignment(program, facilityType,
        fromId(reasonId, StockCardLineItemReason.class)));
  }

  private List<StockCardLineItemReason> getReasons(UUID program, UUID facilityType) {
    return reasonAssignmentRepository
        .findByProgramIdAndFacilityTypeId(program, facilityType)
        .stream().map(ValidReasonAssignment::getReason).collect(toList());
  }

}
