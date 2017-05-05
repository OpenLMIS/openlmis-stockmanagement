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

import static java.util.Arrays.asList;
import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.ADJUSTMENT;
import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.TRANSFER;
import static org.openlmis.stockmanagement.domain.reason.ReasonType.CREDIT;
import static org.openlmis.stockmanagement.domain.reason.ReasonType.DEBIT;

import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReasonConfigurationOptionsController {

  @Autowired
  private PermissionService permissionService;

  @RequestMapping(value = "/reasonTypes", method = RequestMethod.GET)
  public List<ReasonType> getReasonTypes() {
    permissionService.canManageReasons();
    return asList(CREDIT, DEBIT);
  }

  @RequestMapping(value = "/reasonCategories", method = RequestMethod.GET)
  public List<ReasonCategory> getReasonCategories() {
    permissionService.canManageReasons();
    return asList(TRANSFER, ADJUSTMENT);
  }
}
