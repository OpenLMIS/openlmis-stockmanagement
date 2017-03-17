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

import static java.util.Collections.singletonList;
import static org.openlmis.stockmanagement.domain.card.StockCard.createStockCardFrom;
import static org.openlmis.stockmanagement.domain.card.StockCard.newInstanceById;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemsFrom;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StockCardService extends StockCardBaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardService.class);

  @Autowired
  private StockCardRepository cardRepository;

  @Autowired
  private StockCardLineItemRepository stockCardLineItemRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Generate stock card line items and stock cards based on event, and persist them.
   *
   * @param stockEventDto the origin event.
   * @param savedEventId  saved event id.
   * @param currentUserId current user id.
   * @throws IllegalAccessException IllegalAccessException.
   * @throws InstantiationException InstantiationException.
   */
  public void saveFromEvent(StockEventDto stockEventDto, UUID savedEventId, UUID currentUserId)
      throws IllegalAccessException, InstantiationException {

    StockCard stockCard = findExistingOrCreateNewCard(stockEventDto, savedEventId);

    List<StockCardLineItem> lineItems =
        createLineItemsFrom(stockEventDto, stockCard, savedEventId, currentUserId);
    stockCardLineItemRepository.save(lineItems);
    LOGGER.debug("Stock card line item(s) saved");
  }

  /**
   * Find stock card by stock card id.
   *
   * @param stockCardId stock card id.
   * @return the found stock card.
   */
  public StockCardDto findStockCardById(UUID stockCardId) {
    StockCard foundCard = cardRepository.findOne(stockCardId);
    if (foundCard == null) {
      return null;
    }
    LOGGER.debug("Stock card found");

    permissionService.canViewStockCard(foundCard.getProgramId(), foundCard.getFacilityId());
    return createStockCardDtos(singletonList(foundCard)).get(0);
  }

  private StockCard findExistingOrCreateNewCard(StockEventDto eventDto, UUID savedEventId)
      throws InstantiationException, IllegalAccessException {

    UUID foundCardId = cardRepository.getStockCardIdBy(
        eventDto.getProgramId(), eventDto.getFacilityId(), eventDto.getOrderableId());

    if (foundCardId != null) {
      LOGGER.debug("Found existing stock card");
      return newInstanceById(foundCardId);
    } else {
      LOGGER.debug("Creating new stock card");
      StockCard newCard = createStockCardFrom(eventDto, savedEventId);
      return cardRepository.save(newCard);
    }
  }

}
