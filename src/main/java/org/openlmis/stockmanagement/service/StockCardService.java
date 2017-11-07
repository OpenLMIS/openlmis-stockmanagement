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
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemFrom;
import static org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity.identityOf;
import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.PHYSICAL_INVENTORY;

import com.google.common.collect.Lists;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * This class is in charge of persisting and retrieving stock cards. For persisting, it may create
 * and save multiple stock cards in one go, since one stock event may involve more than one
 * orderable/lot combos. For retrieving, it only retrieves one stock card at a time. Its purpose is
 * for users to view one single stock card with full details.
 */
@Service
public class StockCardService extends StockCardBaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardService.class);
  private static final String PHYSICAL_INVENTORY_REASON_PREFIX =
      "stockmanagement.reason.physicalInventory.";

  @Autowired
  private MessageService messageService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private OrderableReferenceDataService orderableRefDataService;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private StockCardRepository cardRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  /**
   * Generate stock card line items and stock cards based on event, and persist them.
   *
   * @param stockEventDto the origin event.
   * @param savedEventId  saved event id.
   */
  void saveFromEvent(StockEventDto stockEventDto, UUID savedEventId) {
    List<StockCard> cardsToUpdate = Lists.newArrayList();

    for (StockEventLineItem eventLineItem : stockEventDto.getLineItems()) {
      StockCard stockCard = findOrCreateCard(stockEventDto, eventLineItem, savedEventId);
      createLineItemFrom(stockEventDto, eventLineItem, stockCard, savedEventId);

      cardsToUpdate.add(stockCard);
    }

    cardRepository.save(cardsToUpdate);
    stockEventDto.getContext().refreshCards();

    LOGGER.debug("Stock cards and line items saved");
  }

  /**
   * Find stock card by stock card id.
   *
   * @param stockCardId stock card id.
   * @return the found stock card.
   */
  public StockCardDto findStockCardById(UUID stockCardId) {
    StockCard card = cardRepository.findOne(stockCardId);
    if (card == null) {
      return null;
    }
    StockCard foundCard = card.shallowCopy();

    LOGGER.debug("Stock card found");
    permissionService.canViewStockCard(foundCard.getProgramId(), foundCard.getFacilityId());

    StockCardDto cardDto = createDtos(singletonList(foundCard)).get(0);
    cardDto.setOrderable(orderableRefDataService.findOne(foundCard.getOrderableId()));
    if (cardDto.hasLot()) {
      cardDto.setLot(lotReferenceDataService.findOne(cardDto.getLot().getId()));
    }
    assignSourceDestinationReasonNameForLineItems(cardDto);
    return cardDto;
  }

  private StockCard findOrCreateCard(StockEventDto eventDto, StockEventLineItem eventLineItem,
                                     UUID savedEventId) {
    StockCard card = eventDto.getContext().findCard(identityOf(eventLineItem));

    if (null == card) {
      card = createStockCardFrom(eventDto, eventLineItem, savedEventId);
    }

    return card;
  }

  private void assignSourceDestinationReasonNameForLineItems(StockCardDto stockCardDto) {
    stockCardDto.getLineItems().forEach(lineItemDto -> {
      StockCardLineItem lineItem = lineItemDto.getLineItem();
      assignReasonName(lineItem);
      lineItemDto.setSource(getFromRefDataOrConvertOrg(lineItem.getSource()));
      lineItemDto.setDestination(getFromRefDataOrConvertOrg(lineItem.getDestination()));
    });
  }

  private void assignReasonName(StockCardLineItem lineItem) {
    boolean isPhysicalReason = lineItem.getReason() != null
        && lineItem.getReason().getReasonCategory() == PHYSICAL_INVENTORY;
    if (isPhysicalReason) {
      String messageKey = PHYSICAL_INVENTORY_REASON_PREFIX
          + lineItem.getReason().getReasonType().toString().toLowerCase();
      String reasonName = messageService.localize(new Message(messageKey)).getMessage();
      lineItem.getReason().setName(reasonName);
    }
  }

  private FacilityDto getFromRefDataOrConvertOrg(Node node) {
    if (node == null) {
      return null;
    }

    if (node.isRefDataFacility()) {
      LOGGER.debug("Calling ref data to retrieve facility info for line item");
      return facilityRefDataService.findOne(node.getReferenceId());
    } else {
      return FacilityDto.createFrom(organizationRepository.findOne(node.getReferenceId()));
    }
  }
}
