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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.HomeFacilityPermissionService;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.JasperTemplateService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.util.ReportUtils;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import java.util.Map;
import java.util.UUID;

/**
 * Controller used to create stock event.
 */
@Controller
@RequestMapping("/api")
public class StockEventsController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventsController.class);
  private static final String PRINT_PI = "Print PI";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private JasperReportService jasperReportService;

  @Autowired
  private JasperTemplateService templateService;

  /**
   * Create stock event.
   *
   * @param eventDto a stock event bound to request body.
   * @return created stock event's ID.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  @RequestMapping(value = "stockEvents", method = POST)
  @Transactional(rollbackFor = {InstantiationException.class, IllegalAccessException.class})
  public ResponseEntity<UUID> createStockEvent(@RequestBody StockEventDto eventDto)
      throws InstantiationException, IllegalAccessException {
    LOGGER.debug("Try to create a stock event");
    checkPermission(eventDto);
    UUID createdEventId = stockEventProcessor.process(eventDto);
    return new ResponseEntity<>(createdEventId, CREATED);
  }

  /**
   * Print out physical inventory as a PDF file.
   *
   * @param id The UUID of the stock event to print
   * @return ResponseEntity with the "#200 OK" HTTP response status and PDF file on success, or
   *     ResponseEntity containing the error description status.
   */
  @RequestMapping(value = "/stockEvents/{id}/print", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView print(@PathVariable("id") UUID id)
      throws JasperReportViewException {
    checkPermission(id);

    JasperTemplate printTemplate = templateService.getByName(PRINT_PI);
    if (printTemplate == null) {
      throw new ValidationMessageException(
        new Message(ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME, PRINT_PI));
    }

    JasperReportsMultiFormatView jasperView =
        jasperReportService.getJasperReportsView(printTemplate);

    return new ModelAndView(jasperView, getParams(id));
  }

  private void checkPermission(StockEventDto eventDto) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();
    if (!authentication.isClientOnly()) {
      UUID programId = eventDto.getProgramId();
      UUID facilityId = eventDto.getFacilityId();

      homeFacilityPermissionService.checkProgramSupported(programId);
      if (eventDto.isPhysicalInventory()) {
        permissionService.canEditPhysicalInventory(programId, facilityId);
      } else {
        //we check STOCK_ADJUST permission for both adjustment and issue/receive
        //this may change in the future
        permissionService.canAdjustStock(programId, facilityId);
      }
    }
  }

  private void checkPermission(UUID id) {
    StockEvent stockEvent = stockEventsRepository.findOne(id);
    if (stockEvent == null) {
      throw new ValidationMessageException(
          new Message(MessageKeys.ERROR_STOCK_EVENT_NOT_FOUND, id));
    }
    permissionService
        .canEditPhysicalInventory(stockEvent.getProgramId(), stockEvent.getFacilityId());
  }

  private Map<String, Object> getParams(UUID eventId) {
    Map<String, Object> params = ReportUtils.createParametersMap();
    String formatId = "'" + eventId + "'";
    params.put("event_id", formatId);
    params.put("subreport",
        jasperReportService.createCustomizedPhysicalInventoryLineSubreport());

    return params;
  }

}
