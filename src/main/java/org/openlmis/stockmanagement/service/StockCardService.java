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
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemsFrom;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StockCardService extends StockCardBaseService {

  @Autowired
  private OrderableReferenceDataService orderableRefDataService;

  @Autowired
  private StockCardRepository stockCardRepository;

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
    StockCardDto stockCardDto = createStockCardDto(singletonList(foundCard)).get(0);
    stockCardDto.setOrderable(orderableRefDataService.findOne(foundCard.getOrderableId()));
    return stockCardDto;
  }

  /**
   * Find stock card summaries by program and facility.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   * @return Stock card summaries.
   */
  public List<StockCardDto> findStockCardSummaries(UUID programId, UUID facilityId) {
    return null;
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
