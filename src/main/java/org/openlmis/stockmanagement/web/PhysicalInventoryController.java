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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME;

import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.JasperTemplateService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.util.ReportUtils;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class PhysicalInventoryController {

  private static final String PRINT_PI = "Print PI";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PhysicalInventoriesRepository repository;

  @Autowired
  private JasperReportService jasperReportService;

  @Autowired
  private JasperTemplateService templateService;

  /**
   * Print out physical inventory as a PDF file.
   *
   * @param id The UUID of the stock event to print
   * @param format The report format
   * @return ResponseEntity with the "#200 OK" HTTP response status and PDF file on success, or
   *     ResponseEntity containing the error description status.
   */
  @GetMapping(value = "/physicalInventories/{id}", params = "format")
  @ResponseBody
  public ModelAndView print(@PathVariable("id") UUID id, @RequestParam String format)
      throws JasperReportViewException {
    checkPermission(id);

    JasperTemplate printTemplate = templateService.getByName(PRINT_PI);
    if (printTemplate == null) {
      throw new ValidationMessageException(
          new Message(ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME, PRINT_PI));
    }

    JasperReportsMultiFormatView jasperView =
        jasperReportService.getJasperReportsView(printTemplate);

    return new ModelAndView(jasperView, getParams(id, format));
  }

  private void checkPermission(UUID id) {
    PhysicalInventory pi = repository.findOne(id);
    if (pi == null) {
      throw new ResourceNotFoundException(
          new Message(MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_FOUND, id));
    }
    permissionService
        .canEditPhysicalInventory(pi.getProgramId(), pi.getFacilityId());
  }

  private Map<String, Object> getParams(UUID eventId, String format) {
    Map<String, Object> params = ReportUtils.createParametersMap();
    String formatId = "'" + eventId + "'";
    params.put("pi_id", formatId);
    params.put("format", format);
    params.put("subreport",
        jasperReportService.createCustomizedPhysicalInventoryLineSubreport());

    return params;
  }

}
