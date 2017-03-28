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

import org.openlmis.stockmanagement.domain.event.StockEvent2;
import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.repository.StockEventsRepository2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * A service that is in charge of saving stock events and generating stock cards
 * and line items from stock events.
 */
@Service
public class StockEventProcessor2 {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventProcessor.class);

  @Autowired
  private StockEventProcessContextBuilder contextBuilder;

  @Autowired
  private StockEventValidationsService2 stockEventValidationsService;

  @Autowired
  private StockCardService2 stockCardService;

  @Autowired
  private StockEventsRepository2 stockEventsRepository;

  /**
   * Validate and persist event and create stock card and line items from it.
   *
   * @param eventDto stock event dto.
   * @return the persisted event ids.
   */

  public UUID process(StockEventDto2 eventDto)
      throws IllegalAccessException, InstantiationException {
    eventDto.setContext(contextBuilder.buildContext2(eventDto));
    stockEventValidationsService.validate(eventDto);
    return saveEventAndGenerateLineItems(eventDto);
  }

  private UUID saveEventAndGenerateLineItems(StockEventDto2 eventDto)
      throws InstantiationException, IllegalAccessException {
    UUID currentUserId = eventDto.getContext().getCurrentUser().getId();

    StockEvent2 stockEvent = eventDto.toEvent(currentUserId);
    UUID savedEventId = stockEventsRepository.save(stockEvent).getId();
    LOGGER.debug("Saved stock event with id " + savedEventId);

    stockCardService.saveFromEvent(eventDto, savedEventId, currentUserId);

    return savedEventId;
  }

}
