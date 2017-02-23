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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class StockCardBaseService {

  @Autowired
  private OrderableReferenceDataService orderableRefDataService;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private ProgramReferenceDataService programRefDataService;

  @Autowired
  private OrganizationRepository organizationRepository;

  protected List<StockCardDto> createStockCardDtos(List<StockCard> stockCards) {
    if (stockCards.isEmpty()) {
      return emptyList();
    }

    stockCards.forEach(StockCard::calculateStockOnHand);

    StockCard firstCard = stockCards.get(0);
    FacilityDto facility = facilityRefDataService.findOne(firstCard.getFacilityId());
    ProgramDto program = programRefDataService.findOne(firstCard.getProgramId());

    return stockCards.stream()
        .map(card -> {
          OrderableDto orderable = orderableRefDataService.findOne(card.getOrderableId());
          return cardToDto(facility, program, orderable, card);
        })
        .collect(toList());
  }

  private StockCardDto cardToDto(FacilityDto facility, ProgramDto program,
                                 OrderableDto orderable, StockCard card) {
    StockCardDto cardDto = StockCardDto.createFrom(card);

    cardDto.setFacility(facility);
    cardDto.setProgram(program);
    cardDto.setOrderable(orderable);

    assignSourceDestinationForLineItems(cardDto);

    return cardDto;
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
}
