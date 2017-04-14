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

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.LotDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StockEventProcessContextBuilder {
  private static final Logger LOGGER = getLogger(StockEventProcessContextBuilder.class);

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
    LOGGER.info("build stock event process context");
    UUID programId = eventDto.getProgramId();
    UUID facilityId = eventDto.getFacilityId();

    return StockEventProcessContext.builder()
        .currentUser(authenticationHelper.getCurrentUser())
        .program(programService.findOne(programId))
        .facility(facilityService.findOne(facilityId))
        .allApprovedProducts(approvedProductService.getAllApprovedProducts(programId, facilityId))
        .lots(getLots(eventDto))
        .build();
  }

  private Map<UUID, LotDto> getLots(StockEventDto eventDto) {
    Stream<UUID> lotIds = eventDto.getLineItems().stream()
        .filter(item -> item.getLotId() != null)
        .map(StockEventLineItem::getLotId);

    return lotIds.collect(HashMap::new,
        (map, lotId) -> map.put(lotId, lotReferenceDataService.findOne(lotId)),
        HashMap::putAll);
  }
}
