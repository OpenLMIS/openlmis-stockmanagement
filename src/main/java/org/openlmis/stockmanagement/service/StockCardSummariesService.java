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
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.openlmis.stockmanagement.service.StockCardSummariesService.SearchOptions.ExistingStockCardsOnly;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.LotDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.util.OrderableLotIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.Getter;

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
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private StockCardRepository cardRepository;

  /**
   * Find all stock cards by program id and facility id.
   *
   * @param programId    program id.
   * @param facilityId   facility id.
   * @param searchOption enum option that indicates either to include approved products
   * @return found stock cards.
   */
  public List<StockCardDto> findStockCards(UUID programId, UUID facilityId,
                                           SearchOptions searchOption) {
    List<StockCard> cards = cardRepository
        .findByProgramIdAndFacilityId(programId, facilityId);

    LOGGER.info("Calling ref data to get approved orderables");

    Map<OrderableLotIdentity, OrderableLot> orderableLotsMap = createOrderableLots(
        approvedProductService.getAllApprovedProducts(programId, facilityId));

    //create dummy(fake/not persisted) cards for approved orderables that don't have cards yet
    Stream<StockCard> dummyCards = createDummyCards(programId, facilityId, cards,
        orderableLotsMap.values(), searchOption);

    List<StockCardDto> dtos =
        createDtos(concat(cards.stream(), dummyCards).collect(toList()));
    return assignOrderableLotRemoveLineItems(dtos, orderableLotsMap);
  }

  /**
   * Get a page of stock cards.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   * @param pageable   page object.
   * @return page of stock cards.
   */
  public Page<StockCardDto> findStockCards(UUID programId, UUID facilityId, Pageable pageable) {
    //Currently this method has not been used,
    // but might be used in the future when do back end pagination
    Page<StockCard> cards = cardRepository
        .findByProgramIdAndFacilityId(programId, facilityId, pageable);

    LOGGER.info("Calling ref data to get approved orderables");
    Map<OrderableLotIdentity, OrderableLot> orderableLotsMap = createOrderableLots(
        approvedProductService.getAllApprovedProducts(programId, facilityId));

    List<StockCardDto> stockCardDtos =
        assignOrderableLotRemoveLineItems(createDtos(cards.getContent()), orderableLotsMap);
    return new PageImpl<>(stockCardDtos, pageable, cards.getTotalElements());
  }

  private List<StockCardDto> assignOrderableLotRemoveLineItems(
      List<StockCardDto> dtos,
      Map<OrderableLotIdentity, OrderableLot> orderableLotsMap) {
    dtos.forEach(stockCardDto -> {
      OrderableLot orderableLot = orderableLotsMap.get(stockCardDto.orderableLotIdentity());
      stockCardDto.setOrderable(orderableLot.getOrderable());
      stockCardDto.setLot(orderableLot.getLot());
      //line items are not needed in summary
      //remove them after re-calculation is done(in base class)
      stockCardDto.setLineItems(null);
    });
    return dtos;
  }

  private Stream<StockCard> createDummyCards(UUID programId, UUID facilityId,
                                             List<StockCard> existingCards,
                                             Collection<OrderableLot> orderableLots,
                                             SearchOptions searchOption) {
    if (searchOption == ExistingStockCardsOnly) {
      return Stream.empty();//do not create dummy cards when option says so.
    }

    return filterOrderableLotsWithoutCards(orderableLots, existingCards)
        .stream()
        .map(orderableLot -> StockCard.builder()
            .programId(programId)
            .facilityId(facilityId)
            .orderableId(orderableLot.getOrderable().getId())
            .lotId(orderableLot.getLotId())
            .lineItems(emptyList())//dummy cards don't have line items
            .build());
  }

  private List<OrderableLot> filterOrderableLotsWithoutCards(
      Collection<OrderableLot> orderableLots, List<StockCard> stockCards) {
    return orderableLots.stream()
        .filter(orderableLot ->
            stockCards.stream().noneMatch(stockCard ->
                stockCard.orderableLotIdentity().equals(orderableLot.orderableLotIdentity())))
        .collect(toList());
  }

  private Map<OrderableLotIdentity, OrderableLot> createOrderableLots(
      List<OrderableDto> orderableDtos) {
    Stream<OrderableLot> orderableLots = orderableDtos.stream().flatMap(orderableDto -> {
      Page<LotDto> lots = lotReferenceDataService.search(orderableDto.getId());
      return lots.getContent().stream().map(l -> new OrderableLot(orderableDto, l));
    });

    Stream<OrderableLot> orderablesOnly = orderableDtos.stream()
        .map(orderableDto -> new OrderableLot(orderableDto, null));

    return concat(orderableLots, orderablesOnly)
        .collect(toMap(OrderableLot::orderableLotIdentity, orderableLot -> orderableLot));
  }

  public enum SearchOptions {
    IncludeApprovedOrderables,
    ExistingStockCardsOnly
  }

  @Getter
  private static class OrderableLot {
    private OrderableDto orderable;
    private LotDto lot;

    OrderableLot(OrderableDto orderable, LotDto lot) {
      this.orderable = orderable;
      this.lot = lot;
    }

    public UUID getLotId() {
      return lot == null ? null : lot.getId();
    }

    public OrderableLotIdentity orderableLotIdentity() {
      return new OrderableLotIdentity(orderable.getId(), lot == null ? null : lot.getId());
    }
  }

}
