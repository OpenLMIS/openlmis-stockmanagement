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

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A service that is in charge of saving stock events and generating stock cards
 * and line items from stock events.
 */
@Service
public class StockEventProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventProcessor.class);

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private StockEventValidationsService stockEventValidationsService;

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  /**
   * Validate and persist event and its line items.
   *
   * @param stockEventDto stock event dto.
   * @return the persisted event's id.
   */
  @Transactional(rollbackFor = {InstantiationException.class, IllegalAccessException.class})
  //the Transactional annotation MUST be on a PUBLIC method
  public UUID process(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {
    LOGGER.debug("Process stock event dto");
    stockEventValidationsService.validate(stockEventDto);

    return saveEventAndGenerateLineItems(stockEventDto);
  }

  private UUID saveEventAndGenerateLineItems(StockEventDto stockEventDto)
      throws InstantiationException, IllegalAccessException {
    UUID currentUserId = authenticationHelper.getCurrentUser().getId();

    StockEvent stockEvent = stockEventDto.toEvent(currentUserId);
    UUID savedEventId = stockEventsRepository.save(stockEvent).getId();
    LOGGER.debug("Saved stock event with id " + savedEventId);

    stockCardService.saveFromEvent(stockEventDto, savedEventId, currentUserId);

    return savedEventId;
  }

}
