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
import static org.springframework.http.HttpStatus.OK;

import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.adjustment.ValidReasonAssignment;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.ProgramFacilityPermissionService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class ValidReasonAssignmentController {
  @Autowired
  private ValidReasonAssignmentRepository validReasonAssignmentRepository;

  @Autowired
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Autowired
  private ProgramFacilityPermissionService programFacilityPermissionService;

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardsController.class);

  /**
   * Get a list of valid reasons.
   * @param program program id.
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
    programFacilityPermissionService.checkProgramFacility(program, facilityType);

    List<ValidReasonAssignment> validReasonAssignments =
        validReasonAssignmentRepository.findByProgramIdAndFacilityTypeId(program, facilityType);
    List<StockCardLineItemReason> lineItemReasons = validReasonAssignments.stream()
        .map(ValidReasonAssignment::getReason).collect(toList());

    return new ResponseEntity<>(lineItemReasons, OK);
  }

}
