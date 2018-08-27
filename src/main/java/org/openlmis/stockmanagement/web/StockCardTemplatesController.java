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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller used for creating/getting stock card templates.
 */
@Controller
@RequestMapping("/api")
public class StockCardTemplatesController {

  @Autowired
  private StockCardTemplateService stockCardTemplateService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Search for stock card template by program id and facility type id.
   *
   * @param program      Program id
   * @param facilityType Facility type id
   * @return The found stock card template, or 404 when not found.
   */
  @RequestMapping(value = "/stockCardTemplates", method = GET)
  public ResponseEntity<StockCardTemplateDto> searchStockCardTemplate(
          @RequestParam(required = false) UUID program,
          @RequestParam(required = false) UUID facilityType) {

    boolean isRequestingForDefaultTemplate = program == null && facilityType == null;

    if (isRequestingForDefaultTemplate) {
      return new ResponseEntity<>(stockCardTemplateService.getDefaultStockCardTemplate(), OK);
    } else {
      return searchForExistingTemplate(program, facilityType);
    }
  }

  /**
   * Create stock card template.
   *
   * @param stockCardTemplate a stock card template bound to request body.
   * @return The created stock card template.
   */
  @RequestMapping(value = "/stockCardTemplates", method = POST)
  public ResponseEntity<StockCardTemplateDto> createStockCardTemplate(
          @RequestBody StockCardTemplateDto stockCardTemplate) {

    permissionService.canCreateStockCardTemplate();
    StockCardTemplateDto savedTemplate = stockCardTemplateService
            .saveOrUpdate(stockCardTemplate);
    return new ResponseEntity<>(savedTemplate, CREATED);
  }

  private ResponseEntity<StockCardTemplateDto> searchForExistingTemplate(
          UUID program, UUID facilityType) {

    StockCardTemplateDto foundTemplate = stockCardTemplateService
            .findByProgramIdAndFacilityTypeId(program, facilityType);

    if (foundTemplate != null) {
      return new ResponseEntity<>(foundTemplate, OK);
    } else {
      return new ResponseEntity<>(NOT_FOUND);
    }
  }
}
