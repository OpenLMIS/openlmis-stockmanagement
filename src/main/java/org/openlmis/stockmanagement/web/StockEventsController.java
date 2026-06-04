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
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventHistoryDto;
import org.openlmis.stockmanagement.dto.StockEventLineDetailDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.service.HomeFacilityPermissionService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.service.StockEventsService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller used to create stock event.
 */
@Controller
@Transactional
@RequestMapping("/api")
public class StockEventsController extends BaseController {
  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventsController.class);

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsService stockEventsService;

  /**
   * Create stock event.
   *
   * @param eventDto a stock event bound to request body.
   * @return created stock event's ID.
   */
  @RequestMapping(value = "stockEvents", method = POST)
  public ResponseEntity<UUID> createStockEvent(@RequestBody StockEventDto eventDto) {
    LOGGER.debug("Try to create a stock event");

    Profiler profiler = getProfiler("CREATE_STOCK_EVENT", eventDto);

    checkPermission(eventDto, profiler.startNested("CHECK_PERMISSION"));

    profiler.start("PROCESS");
    UUID createdEventId = stockEventProcessor.process(eventDto);

    profiler.start("CREATE_RESPONSE");
    ResponseEntity<UUID> response = new ResponseEntity<>(createdEventId, CREATED);

    return stopProfiler(profiler, response);
  }

  /**
   * Get a page of issue/receive stock events (transaction history) for a facility and program.
   *
   * @return a page of stock event history rows.
   */
  @Transactional(readOnly = true)
  @GetMapping("stockEvents")
  @ResponseBody
  public Page<StockEventHistoryDto> getStockEvents(
      @RequestParam UUID facilityId,
      @RequestParam UUID programId,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(required = false) String documentNumber,
      Pageable pageable) {
    permissionService.canViewStockCard(programId, facilityId);

    StockEventSearchParams params = new StockEventSearchParams(
        facilityId, programId, toEventOrigins(type), startDate, endDate, documentNumber);

    return stockEventsService.search(params, pageable);
  }

  // "all"/blank means issue and receive only; new origins are not auto-included.
  private static final Collection<EventOrigin> ALL_HISTORY_ORIGINS =
      Arrays.asList(EventOrigin.ISSUE, EventOrigin.RECEIVE);

  private Collection<EventOrigin> toEventOrigins(String type) {
    if (StringUtils.isBlank(type) || "all".equalsIgnoreCase(type)) {
      return ALL_HISTORY_ORIGINS;
    }
    try {
      return Collections.singletonList(EventOrigin.valueOf(type.toUpperCase()));
    } catch (IllegalArgumentException cause) {
      throw new ValidationMessageException(
          cause, new Message(MessageKeys.ERROR_EVENT_TYPE_INVALID, type));
    }
  }

  /**
   * Get the line items (transaction detail) of a single stock event.
   *
   * @return a page of the event's stock card line item details.
   */
  @Transactional(readOnly = true)
  @GetMapping("stockEvents/{id}/lineItems")
  @ResponseBody
  public Page<StockEventLineDetailDto> getStockEventLineItems(
      @PathVariable UUID id, Pageable pageable) {
    return stockEventsService.findStockEventLineItems(id, pageable);
  }

  private void checkPermission(StockEventDto eventDto, Profiler profiler) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext().getAuthentication();

    if (!authentication.isClientOnly()) {
      UUID programId = eventDto.getProgramId();
      UUID facilityId = eventDto.getFacilityId();

      profiler.start("CHECK_PROGRAM_SUPPORTED_BY_HOME_FACILITY");
      homeFacilityPermissionService.checkProgramSupported(programId);

      if (eventDto.isPhysicalInventory()) {
        profiler.start("CAN_EDIT_PHYSICAL_INVENTORY");
        permissionService.canEditPhysicalInventory(programId, facilityId);
      } else {
        //we check STOCK_ADJUST permission for both adjustment and issue/receive
        //this may change in the future
        profiler.start("CAN_ADJUST_STOCK");
        permissionService.canAdjustStock(programId, facilityId);
      }
    }
  }

}
