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

import org.openlmis.stockmanagement.domain.movement.Organization;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/api")
public class OrganizationController {

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private PermissionService permissionService;

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

  /**
   * Create a new organization.
   *
   * @param organization organization object bound to requist body
   * @return created organization.
   */
  @RequestMapping(value = "organizations", method = RequestMethod.POST)
  public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
    LOGGER.debug("Try to create a new organization.");
    permissionService.canManageOrganizations();
    organization.setId(null);
    return new ResponseEntity<>(organizationRepository.save(organization), HttpStatus.CREATED);
  }
}
