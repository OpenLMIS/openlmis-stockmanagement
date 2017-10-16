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

package org.openlmis.stockmanagement.service;

import static org.slf4j.LoggerFactory.getLogger;
import static org.slf4j.ext.XLoggerFactory.getXLogger;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.slf4j.Logger;
import org.slf4j.ext.XLogger;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Before we process a stock event, this class will run first, to get all things we need from
 * reference data. So that network traffic will be concentrated at one place rather than scattered
 * all around the place.
 */
@Service
public class StockEventProcessContextBuilder {
  private static final Logger LOGGER = getLogger(StockEventProcessContextBuilder.class);
  private static final XLogger XLOGGER = getXLogger(StockEventProcessContextBuilder.class);

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private FacilityReferenceDataService facilityService;

  @Autowired
  private ProgramReferenceDataService programService;

  @Autowired
  private ApprovedProductReferenceDataService approvedProductService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  /**
   * Before processing events, put all needed ref data into context so we don't have to do frequent
   * network requests.
   *
   * @param eventDto event dto.
   * @return a context object that includes all needed ref data.
   */
  public StockEventProcessContext buildContext(StockEventDto eventDto) {
    XLOGGER.entry(eventDto);
    Profiler profiler = new Profiler("BUILD_CONTEXT");
    profiler.setLogger(XLOGGER);

    LOGGER.info("build stock event process context");
    profiler.start("CREATE_BUILDER");
    StockEventProcessContext.StockEventProcessContextBuilder builder = StockEventProcessContext
        .builder();

    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    if (authentication.isClientOnly()) {
      profiler.start("GET_USER");
      builder.currentUserId(eventDto.getUserId());
    } else {
      profiler.start("GET_CURRENT_USER");
      builder.currentUserId(authenticationHelper.getCurrentUser().getId());
    }

    profiler.start("GET_PROGRAM");
    UUID programId = eventDto.getProgramId();
    ProgramDto program = programService.findOne(programId);

    profiler.start("GET_FACILITY");
    UUID facilityId = eventDto.getFacilityId();
    FacilityDto facility = facilityService.findOne(facilityId);

    profiler.start("GET_APPROVED_PRODUCTS");
    List<OrderableDto> approvedProducts = approvedProductService
        .getAllApprovedProducts(programId, facilityId);

    profiler.start("GET_LOTS");
    Map<UUID, LotDto> lots = getLots(eventDto);

    profiler.start("BUILD");
    StockEventProcessContext context = builder
        .program(program)
        .facility(facility)
        .allApprovedProducts(approvedProducts)
        .lots(lots)
        .build();

    profiler.stop().log();
    XLOGGER.exit(context);

    return context;
  }

  private Map<UUID, LotDto> getLots(StockEventDto eventDto) {
    return eventDto
        .getLineItems()
        .stream()
        .filter(item -> item.getLotId() != null)
        .map(StockEventLineItem::getLotId)
        .distinct()
        .collect(Collectors.toMap(id -> id, id -> lotReferenceDataService.findOne(id)));
  }
}
