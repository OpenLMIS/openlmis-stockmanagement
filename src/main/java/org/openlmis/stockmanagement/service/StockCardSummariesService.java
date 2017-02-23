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

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class StockCardSummariesService extends StockCardBaseService {

  @Autowired
  private ApprovedProductReferenceDataService approvedProductRefDataService;

  @Autowired
  private StockCardRepository stockCardRepository;

  /**
   * Find stock card by program id and facility id.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   * @return found stock cards, will include approved products without stock cards.
   */
  public List<StockCardDto> findStockCards(UUID programId, UUID facilityId) {
    List<ApprovedProductDto> approvedProducts = approvedProductRefDataService
        .getAllApprovedProducts(programId, facilityId);

    List<StockCard> stockCards = stockCardRepository
        .findByProgramIdAndFacilityId(programId, facilityId);

    List<ApprovedProductDto> productsWithoutCards =
        filterProductsWithoutCards(approvedProducts, stockCards);

    List<StockCardDto> productCardDtos =
        productsToCardDtos(programId, facilityId, productsWithoutCards);

    productCardDtos.forEach(productCardDto -> productCardDto.setStockOnHand(null));

    List<StockCardDto> cardDtos = createStockCardDtos(stockCards);

    List<StockCardDto> allCardDtos = concat(cardDtos.stream(), productCardDtos.stream())
        .collect(toList());
    allCardDtos.forEach(cardDto -> cardDto.setLineItems(null));
    return allCardDtos;
  }

  private List<ApprovedProductDto> filterProductsWithoutCards(
      List<ApprovedProductDto> approvedProductDtos, List<StockCard> stockCards) {
    return approvedProductDtos.stream()
        .filter(approvedProductDto -> stockCards.stream()
            .noneMatch(stockCard -> stockCard.getOrderableId()
                .equals(approvedProductDto.getProgramOrderable().getOrderableId())))
        .collect(toList());
  }

  private List<StockCardDto> productsToCardDtos(
      UUID programId, UUID facilityId,
      Collection<ApprovedProductDto> approvedProducts) {
    List<StockCard> cards = approvedProducts
        .stream().map(approvedProduct -> {
          StockCard card = new StockCard();
          card.setLineItems(emptyList());
          card.setProgramId(programId);
          card.setFacilityId(facilityId);
          card.setOrderableId(approvedProduct.getProgramOrderable().getOrderableId());
          return card;
        }).collect(toList());

    return createStockCardDtos(cards);
  }

}
