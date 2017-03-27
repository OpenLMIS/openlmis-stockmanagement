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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.ValidDestinationService;
import org.openlmis.stockmanagement.service.ValidSourceService;
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
public class ValidSourceDestinationController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ValidSourceDestinationController.class);

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ValidSourceService validSourceService;

  @Autowired
  private ValidDestinationService validDestinationService;

  /**
   * Get a list of valid destinations.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   * @return found valid destinations.
   */
  @RequestMapping(value = "/validDestinations", method = GET)
  public ResponseEntity<List<ValidSourceDestinationDto>> getValidDestinations(
      @RequestParam UUID program,
      @RequestParam UUID facilityType) {
    LOGGER.debug(format("Try to find valid destinations with program %s and facility type %s",
        program.toString(), facilityType.toString()));
    permissionService.canViewValidDestinations(program, facilityType);
    return new ResponseEntity<>(
        validDestinationService.findDestinations(program, facilityType), OK);
  }

  /**
   * Assign a destination to a program and facility type.
   * If valid destination assignment ID is specified, ID will be ignored.
   *
   * @return the assigned destination and program and facility type.
   */
  @RequestMapping(value = "/validDestinations", method = POST)
  public ResponseEntity<ValidSourceDestinationDto> assignDestination(
      @RequestBody ValidDestinationAssignment assignment) {
    LOGGER.debug("Try to assign destinations");
    permissionService.canManageStockDestinations();

    ValidSourceDestinationDto destinationDto = validDestinationService
        .findByProgramFacilityDestination(assignment);
    if (destinationDto != null) {
      return new ResponseEntity<>(destinationDto, OK);
    }

    return new ResponseEntity<>(
        validDestinationService.assignDestination(assignment), CREATED);
  }

  /**
   * Get a list of valid sources.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   * @return found valid sources.
   */
  @RequestMapping(value = "/validSources", method = GET)
  public ResponseEntity<List<ValidSourceDestinationDto>> getValidSources(
      @RequestParam UUID program,
      @RequestParam UUID facilityType) {
    LOGGER.debug(format("Try to find valid sources with program %s and facility type %s",
        program.toString(), facilityType.toString()));
    permissionService.canViewValidDestinations(program, facilityType);
    return new ResponseEntity<>(
        validSourceService.findSources(program, facilityType), OK);
  }

  /**
   * Assign a source to program and facility type.
   * If valid source assignment ID is specified, ID will be ignored.
   *
   * @return the assigned source and program and facility type.
   */
  @RequestMapping(value = "/validSources", method = POST)
  public ResponseEntity<ValidSourceDestinationDto> assignSource(
      @RequestBody ValidSourceAssignment assignment) {
    LOGGER.debug("Try to assign source");
    permissionService.canManageStockSources();

    ValidSourceDestinationDto foundAssignmentDto = validSourceService
        .findByProgramFacilitySource(assignment);
    if (foundAssignmentDto != null) {
      return new ResponseEntity<>(foundAssignmentDto, OK);
    }
    return new ResponseEntity<>(
        validSourceService.assignSource(assignment), CREATED);
  }

  /**
   * Remove a valid source assignment of a program and facility type combination.
   *
   * @param assignmentId source assignment ID
   * @return no content status
   */
  @RequestMapping(value = "/validSources/{id}", method = DELETE)
  public ResponseEntity removeValidSourceAssignment(@PathVariable("id") UUID assignmentId) {
    LOGGER.debug(format("Try to remove source assignment %s.", assignmentId));
    permissionService.canManageStockSources();
    validSourceService.deleteSourceAssignmentById(assignmentId);
    return new ResponseEntity(null, NO_CONTENT);
  }

  /**
   * Remove a valid destination assignment of a program and facility type combination.
   *
   * @param assignmentId destination assignment ID
   * @return no content status
   */
  @RequestMapping(value = "/validDestinations/{id}", method = DELETE)
  public ResponseEntity removeValidDestinationAssignment(@PathVariable("id") UUID assignmentId) {
    LOGGER.debug(format("Try to remove destination assignment %s.", assignmentId));
    permissionService.canManageStockDestinations();
    validDestinationService.deleteDestinationAssignmentById(assignmentId);
    return new ResponseEntity(null, NO_CONTENT);
  }
}
