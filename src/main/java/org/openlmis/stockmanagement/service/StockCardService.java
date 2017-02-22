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

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.openlmis.stockmanagement.domain.card.StockCard.createStockCardFrom;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemsFrom;

@Service
public class StockCardService {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private ProgramReferenceDataService programRefDataService;

  @Autowired
  private OrderableReferenceDataService orderableRefDataService;

  @Autowired
  private OrganizationRepository organizationRepository;

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

    createLineItemsFrom(stockEventDto, stockCard, savedEventId, currentUserId);
    stockCardRepository.save(stockCard);
  }

  /**
   * Find stock card by stock card id.
   *
   * @param stockCardId stock card id.
   * @return the found stock card.
   */
  public StockCardDto findStockCardById(UUID stockCardId) {
    StockCard foundCard = stockCardRepository.findOne(stockCardId);
    if (foundCard == null) {
      return null;
    }

    permissionService.canViewStockCard(foundCard.getProgramId(), foundCard.getFacilityId());
    return createStockCardDto(foundCard);
  }

  private StockCardDto createStockCardDto(StockCard foundCard) {
    foundCard.calculateStockOnHand();
    StockCardDto stockCardDto = StockCardDto.createFrom(foundCard);

    assignFacilityProgramOrderableForStockCard(foundCard, stockCardDto);
    assignSourceDestinationForLineItems(stockCardDto);

    return stockCardDto;
  }

  private void assignFacilityProgramOrderableForStockCard(
      StockCard foundCard, StockCardDto stockCardDto) {
    stockCardDto.setFacility(facilityRefDataService.findOne(foundCard.getFacilityId()));
    stockCardDto.setProgram(programRefDataService.findOne(foundCard.getProgramId()));
    stockCardDto.setOrderable(orderableRefDataService.findOne(foundCard.getOrderableId()));
  }

  private void assignSourceDestinationForLineItems(StockCardDto stockCardDto) {
    stockCardDto.getLineItems().forEach(lineItemDto -> {
      StockCardLineItem lineItem = lineItemDto.getLineItem();
      lineItemDto.setSource(getFromRefDataOrConvertOrg(lineItem.getSource()));
      lineItemDto.setDestination(getFromRefDataOrConvertOrg(lineItem.getDestination()));
    });
  }

  private FacilityDto getFromRefDataOrConvertOrg(Node node) {
    if (node == null) {
      return null;
    }

    if (node.isRefDataFacility()) {
      return facilityRefDataService.findOne(node.getReferenceId());
    } else {
      return FacilityDto.createFrom(organizationRepository.findOne(node.getReferenceId()));
    }
  }

  private StockCard findExistingOrCreateNewCard(StockEventDto stockEventDto, UUID savedEventId)
      throws InstantiationException, IllegalAccessException {

    StockCard foundCard = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableId(
        stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(),
        stockEventDto.getOrderableId());

    if (foundCard != null) {
      return foundCard;
    } else {
      return createStockCardFrom(stockEventDto, savedEventId);
    }
  }
}
