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

import static java.util.stream.Collectors.groupingBy;
import static org.openlmis.stockmanagement.dto.PhysicalInventoryDto.fromEventDto;
import static org.openlmis.stockmanagement.i18n.MessageKeys.SERVER_ERROR_SHALLOW_COPY;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A service that is in charge of saving stock events and generating stock cards
 * and line items from stock events.
 */
@Service
public class StockEventProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventProcessor.class);

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

  public UUID process(StockEventDto eventDto)
      throws IllegalAccessException, InstantiationException {
    eventDto.setContext(contextBuilder.buildContext(eventDto));
    stockEventValidationsService.validate(eventDto);
    return saveEventAndGenerateLineItems(eventDto);
  }

  private UUID saveEventAndGenerateLineItems(StockEventDto eventDto)
      throws InstantiationException, IllegalAccessException {
    UUID currentUserId = eventDto.getContext().getCurrentUser().getId();

    StockEvent stockEvent = eventDto.toEvent(currentUserId);
    UUID savedEventId = stockEventsRepository.save(stockEvent).getId();
    LOGGER.debug("Saved stock event with id " + savedEventId);

    if (eventDto.isPhysicalInventory()) {
      physicalInventoryService.submitPhysicalInventory(fromEventDto(eventDto), savedEventId);
    }
    stockCardService.saveFromEvent(eventDto, savedEventId, currentUserId);

    Map<OrderableLotIdentity, List<StockEventLineItem>> sameOrderableGroups = eventDto
        .getLineItems().stream()
        .collect(groupingBy(OrderableLotIdentity::identityOf));

    sameOrderableGroups.values().forEach(group -> callNotifications(eventDto, group));

    return savedEventId;
  }

  private void callNotifications(StockEventDto event, List<StockEventLineItem> groupItems) {
    StockCard foundCard = tryFindCard(
        event.getProgramId(),
        event.getFacilityId(),
        groupItems.get(0)
    );
    for (StockCardLineItem line : foundCard.getLineItems()) {
      StockCardLineItemReason reason = line.getReason();
      if (reason != null) {
        line.setReason(reasonRepository.findOne(reason.getId()));
      }
    }
    foundCard.calculateStockOnHand();

    if (foundCard.getStockOnHand() == 0) {
      stockoutNotifier.notifyStockEditors(foundCard);
    }
  }

  private StockCard tryFindCard(UUID programId, UUID facilityId, StockEventLineItem lineItem) {
    StockCard foundCard = stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableIdAndLotId(programId, facilityId,
            lineItem.getOrderableId(), lineItem.getLotId());
    //use a shallow copy of stock card to do recalculation, because some domain model will be
    //modified during recalculation, this will avoid persistence of those modified models
    try {
      StockCard stockCard = foundCard.shallowCopy();
      stockCard.setId(foundCard.getId());
      setGroupingFields(programId, facilityId, lineItem, stockCard);
      return stockCard;
    } catch (InvocationTargetException | NoSuchMethodException
        | InstantiationException | IllegalAccessException ex) {
      //if this exception is ever seen in front end, that means our code has a bug. we only put
      //this here to satisfy checkstyle/pmd and to make sure potential bug is not hidden.
      throw new ValidationMessageException(new Message(SERVER_ERROR_SHALLOW_COPY, ex));
    }
  }

  private void setGroupingFields(UUID programId, UUID facilityId, StockEventLineItem lineItem,
                                 StockCard card) {
    card.setFacilityId(facilityId);
    card.setProgramId(programId);
    card.setOrderableId(lineItem.getOrderableId());
    card.setLotId(lineItem.getLotId());
  }

}
