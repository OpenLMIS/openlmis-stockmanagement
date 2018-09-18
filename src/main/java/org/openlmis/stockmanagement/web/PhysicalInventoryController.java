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

import static java.lang.String.join;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_FORMAT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.JasperTemplateService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

@Controller
@RequestMapping("/api/physicalInventories")
public class PhysicalInventoryController {

  public static final String PRINT_PI = "Print PI";
  public static final String ID_PATH_VARIABLE = "/{id}";

  @Autowired
  private PhysicalInventoriesRepository repository;

  @Autowired
  private JasperReportService jasperReportService;

  @Autowired
  private JasperTemplateService templateService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PhysicalInventoryService physicalInventoryService;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoryRepository;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${time.zoneId}")
  private String timeZoneId;

  /**
   * Get a draft physical inventory.
   *
   * @param program  program ID.
   * @param facility facility ID.
   * @return returns found draft, if not found, returns empty draft.
   */
  @RequestMapping(method = GET)
  public ResponseEntity<List<PhysicalInventoryDto>> searchPhysicalInventory(
      @RequestParam UUID program,
      @RequestParam UUID facility,
      @RequestParam(required = false) Boolean isDraft) {
    physicalInventoryService.checkPermission(program, facility);
    List<PhysicalInventoryDto> inventories =
        physicalInventoryService.findPhysicalInventory(program, facility, isDraft);

    return new ResponseEntity<>(inventories, OK);
  }

  /**
   * Get a physical inventory.
   *
   * @param id  physical inventory id.
   * @return returns found physical inventory, if not found, returns No Content.
   */
  @GetMapping(ID_PATH_VARIABLE)
  @ResponseStatus(OK)
  @ResponseBody
  public PhysicalInventoryDto getPhysicalInventory(@PathVariable UUID id) {
    PhysicalInventory foundInventory = physicalInventoryRepository.findOne(id);
    if (foundInventory != null) {
      physicalInventoryService.checkPermission(
          foundInventory.getProgramId(), foundInventory.getFacilityId());
      return PhysicalInventoryDto.from(foundInventory);
    } else {
      throw new ResourceNotFoundException(new Message(ERROR_PHYSICAL_INVENTORY_NOT_FOUND, id));
    }
  }

  /**
   * Creates an empty physical inventory.
   *
   * @return created physical inventory dto.
   */
  @Transactional
  @PostMapping
  @ResponseStatus(CREATED)
  @ResponseBody
  public PhysicalInventoryDto createEmptyPhysicalInventory(
      @RequestBody PhysicalInventoryDto dto) {
    return physicalInventoryService.createNewDraft(dto);
  }

  /**
   * Save a physical inventory.
   *
   * @param id physical inventory id.
   * @param dto physical inventory dto.
   * @return created physical inventory dto.
   */
  @Transactional
  @PutMapping(ID_PATH_VARIABLE)
  @ResponseStatus(OK)
  @ResponseBody
  public PhysicalInventoryDto savePhysicalInventory(@PathVariable UUID id,
                                                    @RequestBody PhysicalInventoryDto dto) {
    return physicalInventoryService.saveDraft(dto, id);
  }

  /**
   * Delete a draft physical inventory.
   *
   * @param id physical inventory id.
   */
  @DeleteMapping(ID_PATH_VARIABLE)
  @ResponseStatus(NO_CONTENT)
  public void deletePhysicalInventory(@PathVariable UUID id) {
    physicalInventoryService.deletePhysicalInventory(id);
  }

  /**
   * Print out physical inventory as a PDF file.
   *
   * @param id The UUID of the stock event to print
   * @param format The report format
   * @return ResponseEntity with the "#200 OK" HTTP response status and PDF file on success, or
   *     ResponseEntity containing the error description status.
   */
  @GetMapping(value = ID_PATH_VARIABLE, params = "format")
  @ResponseBody
  public ModelAndView print(@PathVariable("id") UUID id, @RequestParam String format)
      throws JasperReportViewException {
    checkPermission(id);
    checkFormat(format.toLowerCase());

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
          new Message(ERROR_PHYSICAL_INVENTORY_NOT_FOUND, id));
    }
    permissionService.canEditPhysicalInventory(pi.getProgramId(), pi.getFacilityId());
  }

  private void checkFormat(String format) {
    List<String> supportedFormats = Arrays.asList("csv", "html", "pdf", "xls", "xlsx");
    if (!supportedFormats.contains(format)) {
      throw new ResourceNotFoundException(
          new Message(ERROR_PHYSICAL_INVENTORY_FORMAT_NOT_ALLOWED,
              format,
              join(", ", supportedFormats)));
    }
  }

  private Map<String, Object> getParams(UUID eventId, String format) {
    Map<String, Object> params = ReportUtils.createParametersMap();
    String formatId = "'" + eventId + "'";
    params.put("pi_id", formatId);
    params.put("dateTimeFormat", dateTimeFormat);
    params.put("dateFormat", dateFormat);
    params.put("timeZoneId", timeZoneId);
    params.put("format", format);
    params.put("subreport",
        jasperReportService.createCustomizedPhysicalInventoryLineSubreport());

    return params;
  }

}
