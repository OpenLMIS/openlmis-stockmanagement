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

import static java.util.stream.Collectors.toMap;
import static org.openlmis.stockmanagement.dto.PhysicalInventoryDto.fromEventDto;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
  private StockoutNotifier stockoutNotifier;
  @Autowired
  private StockCardRepository stockCardRepository;
  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

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
    }

    profiler.start("SAVE_FROM_EVENT");
    stockCardService.saveFromEvent(eventDto, savedEventId);

    profiler.start("GET_CARDS_FOR_PROGRAM_AND_FACILITY");
    List<StockCard> cards = stockCardRepository
        .findByProgramIdAndFacilityId(eventDto.getProgramId(), eventDto.getFacilityId());

    Map<UUID, StockCardLineItemReason> reasons = getReasons(
        cards, profiler.startNested("GET_REASONS")
    );

    profiler.start("GROUP_CARDS_BY_IDENTITY");
    Map<OrderableLotIdentity, StockCard> groupCardsByIdentity = cards
        .stream()
        //use a shallow copy of stock card to do recalculation, because some domain model will be
        //modified during recalculation, this will avoid persistence of those modified models
        .map(StockCard::shallowCopy)
        .collect(toMap(OrderableLotIdentity::identityOf, card -> card));

    profiler.start("CALL_NOTIFICATIONS");
    eventDto
        .getLineItems()
        .stream()
        .map(OrderableLotIdentity::identityOf)
        .map(groupCardsByIdentity::get)
        .forEach(card -> callNotifications(card, reasons));

    return savedEventId;
  }

  private void callNotifications(StockCard foundCard, Map<UUID, StockCardLineItemReason> reasons) {
    for (StockCardLineItem line : foundCard.getLineItems()) {
      StockCardLineItemReason reason = line.getReason();

      if (null != reason) {
        UUID key = reason.getId();
        line.setReason(reasons.get(key));
      }
    }

    foundCard.calculateStockOnHand();

    if (foundCard.getStockOnHand() == 0) {
      stockoutNotifier.notifyStockEditors(foundCard);
    }
  }

  private Map<UUID, StockCardLineItemReason> getReasons(List<StockCard> cards, Profiler profiler) {
    profiler.start("GET_IDS_FROM_CARDS");
    Set<UUID> reasonIds = cards
        .stream()
        .map(StockCard::getLineItems)
        .flatMap(Collection::stream)
        .map(StockCardLineItem::getReason)
        .filter(Objects::nonNull)
        .map(StockCardLineItemReason::getId)
        .collect(Collectors.toSet());

    profiler.start("DB_CALL");
    List<StockCardLineItemReason> reasons = reasonRepository.findByIdIn(reasonIds);

    profiler.start("GROUP_BY_ID");
    return reasons
        .stream()
        .collect(Collectors.toMap(StockCardLineItemReason::getId, reason -> reason));
  }

}
