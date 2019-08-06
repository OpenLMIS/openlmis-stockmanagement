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
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.LocalDate;
import java.util.List;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This base class is in charge of:
 * 1. assign facility and program to stock card dto
 * 2. re-calculating soh for stock card and line items
 * It does not handle assigning orderable dto to stock card dto, that is expected to be done in sub
 * classes, potentially in different ways. It also does not handle assigning facility dto to line
 * items, that is not needed by all sub classes.
 */
@Service
public abstract class StockCardBaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardBaseService.class);

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private ProgramReferenceDataService programRefDataService;
  
  @Autowired
  protected CalculatedStockOnHandService calculatedStockOnHandService;

  protected List<StockCardDto> createDtos(List<StockCard> stockCards) {
    if (stockCards.isEmpty()) {
      return emptyList();
    }

    StockCard firstCard = stockCards.get(0);

    LOGGER.debug("Calling ref data to retrieve facility info for card");
    FacilityDto facility = facilityRefDataService.findOne(firstCard.getFacilityId());
    LOGGER.debug("Calling ref data to retrieve program info for card");
    ProgramDto program = programRefDataService.findOne(firstCard.getProgramId());

    return stockCards.stream()
        .map(card -> cardToDto(facility, program, card))
        .collect(toList());
  }

  private StockCardDto cardToDto(FacilityDto facility, ProgramDto program,
                                 StockCard card) {
    
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(card, LocalDate.now());
    StockCardDto cardDto = StockCardDto.createFrom(card);
    
    cardDto.setFacility(facility);
    cardDto.setProgram(program);
    cardDto.setOrderable(OrderableDto.builder().id(card.getOrderableId()).build());
    if (card.getLotId() != null) {
      cardDto.setLot(LotDto.builder().id(card.getLotId()).build());
    }
    List<StockCardLineItemDto> lineItems = cardDto.getLineItems();
    if (!isEmpty(lineItems)) {
      cardDto.setLastUpdate(card.getOccurredDate());
      cardDto.setExtraData(lineItems.get(lineItems.size() - 1).getLineItem().getExtraData());
    }

    return cardDto;
  }

}
