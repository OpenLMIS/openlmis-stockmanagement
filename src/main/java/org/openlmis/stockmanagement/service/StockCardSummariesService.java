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
import static java.util.stream.Stream.concat;
import static org.openlmis.stockmanagement.service.StockCardSummariesService.SearchOptions.ExistingStockCardsOnly;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StockCardSummariesService extends StockCardBaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardSummariesService.class);

  @Autowired
  private ApprovedProductReferenceDataService approvedProductService;

  @Autowired
  private StockCardRepository cardRepository;

  /**
   * Find stock card by program id and facility id.
   *
   * @param programId    program id.
   * @param facilityId   facility id.
   * @param searchOption enum option that indicates either to include approved products
   * @return found stock cards.
   */
  public List<StockCardDto> findStockCards(UUID programId, UUID facilityId,
                                           SearchOptions searchOption) {
    List<StockCard> cards = cardRepository.findByProgramIdAndFacilityId(programId, facilityId);

    LOGGER.info("Calling ref data to get approved orderables");
    Map<UUID, OrderableDto> approvedMap = approvedProductService
        .getApprovedOrderablesMap(programId, facilityId);

    //create dummy(fake/not persisted) cards for approved orderables that don't have cards yet
    Stream<StockCard> dummyCards =
        createDummyCards(programId, facilityId, cards, approvedMap, searchOption);

    List<StockCardDto> dtos = createDtos(concat(cards.stream(), dummyCards).collect(toList()));
    return assignOrderableRemoveLineItems(dtos, approvedMap);
  }

  private List<StockCardDto> assignOrderableRemoveLineItems(List<StockCardDto> dtos,
                                                            Map<UUID, OrderableDto> approvedMap) {
    dtos.forEach(dto -> {
      dto.setOrderable(approvedMap.get(dto.getOrderable().getId()));
      //line items are not needed in summary
      //remove them after re-calculation is done(in base class)
      dto.setLineItems(null);
    });
    return dtos;
  }

  private Stream<StockCard> createDummyCards(UUID programId, UUID facilityId,
                                             List<StockCard> existingCards,
                                             Map<UUID, OrderableDto> approvedOrderablesMap,
                                             SearchOptions searchOption) {
    if (searchOption == ExistingStockCardsOnly) {
      return Stream.empty();//do not create dummy cards when option says so.
    }

    return filterProductsWithoutCards(approvedOrderablesMap.values(), existingCards)
        .stream()
        .map(orderableDto -> StockCard.builder()
            .programId(programId)
            .facilityId(facilityId)
            .orderableId(orderableDto.getId())
            .lineItems(emptyList())//dummy cards don't have line items
            .build());
  }

  private List<OrderableDto> filterProductsWithoutCards(
      Collection<OrderableDto> approvedOrderables, List<StockCard> stockCards) {
    return approvedOrderables.stream()
        .filter(approvedOrderable -> stockCards.stream().noneMatch(
            stockCard -> stockCard.getOrderableId().equals(approvedOrderable.getId())))
        .collect(toList());
  }

  public enum SearchOptions {
    IncludeApprovedOrderables,
    ExistingStockCardsOnly
  }

}
