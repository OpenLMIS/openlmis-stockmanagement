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

import static org.openlmis.stockmanagement.dto.PhysicalInventoryDto.fromEventDto;

import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.extension.ExtensionManager;
import org.openlmis.stockmanagement.extension.point.ExtensionPointId;
import org.openlmis.stockmanagement.extension.point.StockEventPostProcessor;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service that is in charge of saving stock events and generating stock cards and line items from
 * stock events.
 */
@Service
public class StockEventProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventProcessor.class);
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(StockEventProcessor.class);

  @Autowired
  private StockEventProcessContextBuilder contextBuilder;

  @Autowired
  private StockEventValidationsService stockEventValidationsService;

  @Autowired
  private PhysicalInventoryService physicalInventoryService;

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockEventNotificationProcessor stockEventNotificationProcessor;

  @Autowired
  private ExtensionManager extensionManager;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Validate and persist event and create stock card and line items from it.
   *
   * @param eventDto stock event dto.
   * @return the persisted event ids.
   */

  public UUID process(StockEventDto eventDto) {
    XLOGGER.entry(eventDto);
    Profiler profiler = new Profiler("PROCESS");
    profiler.setLogger(XLOGGER);

    profiler.start("BUILD_CONTEXT");
    StockEventProcessContext context = contextBuilder.buildContext(eventDto);
    eventDto.setContext(context);

    profiler.start("VALIDATE");
    stockEventValidationsService.validate(eventDto);

    UUID eventId = saveEventAndGenerateLineItems(
        eventDto, profiler.startNested("SAVE_AND_GENERATE_LINE_ITEMS")
    );

    StockEventPostProcessor stockEventPostProcessor = extensionManager.getExtension(
        ExtensionPointId.STOCK_EVENT_POINT_ID, StockEventPostProcessor.class);
    stockEventPostProcessor.process(eventDto);

    profiler.stop().log();
    XLOGGER.exit(eventId);

    return eventId;
  }

  private UUID saveEventAndGenerateLineItems(StockEventDto eventDto, Profiler profiler) {
    profiler.start("CONVERT_TO_EVENT");
    StockEvent stockEvent = eventDto.toEvent();

    profiler.start("DB_SAVE");
    UUID savedEventId = stockEventsRepository.save(stockEvent).getId();
    LOGGER.debug("Saved stock event with id " + savedEventId);

    if (eventDto.isPhysicalInventory()) {
      profiler.start("CREATE_PHYSICAL_INVENTORY_DTO");
      PhysicalInventoryDto inventoryDto = fromEventDto(eventDto);

      profiler.start("SUBMIT_PHYSICAL_INVENTORY");
      physicalInventoryService.submitPhysicalInventory(inventoryDto, savedEventId);

      profiler.start("FLUSH");
      entityManager.flush();
      entityManager.clear();
    }

    stockCardService.saveFromEvent(eventDto, savedEventId, profiler.startNested("SAVE_FROM_EVENT"));

    profiler.start("CALL_NOTIFICATIONS");
    stockEventNotificationProcessor.callAllNotifications(eventDto);

    return savedEventId;
  }
}
