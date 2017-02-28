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

import static org.springframework.http.HttpStatus.OK;

import org.openlmis.stockmanagement.dto.ValidDestinationAssignmentDto;
import org.openlmis.stockmanagement.service.ValidSourceDestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class ValidSourceDestinationController {

  @Autowired
  ValidSourceDestinationService validSourceDestinationService;

  @RequestMapping(value = "/validDestinations")
  public ResponseEntity<List<ValidDestinationAssignmentDto>> getValidDestinations(
      @RequestParam UUID program,
      @RequestParam UUID facilityType) {
    return new ResponseEntity<>(
        validSourceDestinationService.findValidDestinations(program, facilityType), OK);
  }
}
