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
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity.identityOf;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderablesAggregator;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableFulfillReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * This class is in charge of retrieving stock card summaries(stock cards with soh but not line
 * items).
 * Its result may include existing stock cards only, or it may include dummy stock cards for
 * approved products and their lots. See SearchOptions for details.
 */
@Service
@SuppressWarnings("PMD.TooManyMethods")
public class StockCardSummariesService extends StockCardBaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardSummariesService.class);

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private OrderableFulfillReferenceDataService orderableFulfillService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Get a map of stock cards assigned to orderable ids.
   * Stock cards are grouped using orderable fulfills endpoint.
   * If there is no orderable that can be fulfilled by stock card its orderable id will be used.
   *
   * @param programId    UUID of the program
   * @param facilityId   UUID of the facility
   * @param orderableIds collection of unique orderable UUIDs
   * @return map of stock cards assigned to orderable ids
   */
  public Map<UUID, StockCardAggregate> getGroupedStockCards(UUID programId, UUID facilityId,
      Set<UUID> orderableIds, LocalDate startDate, LocalDate endDate) {
    List<StockCard> stockCards = calculatedStockOnHandService
        .getStockCardsWithStockOnHand(programId, facilityId);

    Map<UUID, OrderableFulfillDto> orderableFulfillMap =
        orderableFulfillService.findByIds(stockCards.stream()
            .map(StockCard::getOrderableId)
            .collect(toSet()));

    return stockCards.stream()
        .map(stockCard -> assignOrderableToStockCard(
            stockCard, orderableFulfillMap, orderableIds, startDate, endDate))
        .filter(pair -> null != pair.getLeft())
        .collect(toMap(
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            (StockCardAggregate aggregate1, StockCardAggregate aggregate2) -> {
              aggregate1.getStockCards().addAll(aggregate2.getStockCards());
              return aggregate1;
            }));
  }

  /**
   * Get a page of stock cards.
   *
   * @param params stock cards summaries search params.
   * @return page of stock cards.
   */
  public StockCardSummaries findStockCards(StockCardSummariesV2SearchParams params) {
    Profiler profiler = new Profiler("FIND_STOCK_CARD_SUMMARIES_FOR_PARAMS");
    profiler.setLogger(LOGGER);

    profiler.start("VALIDATE_VIEW_RIGHTS");
    permissionService.canViewStockCard(params.getProgramId(), params.getFacilityId());

    profiler.start("GET_APPROVED_PRODUCTS");
    OrderablesAggregator approvedProducts = approvedProductReferenceDataService
        .getApprovedProducts(params.getFacilityId(), params.getProgramId(),
            params.getOrderableIds(), params.getOrderableCode(), params.getOrderableName()
        );

    profiler.start("FIND_ORDERABLE_FULFILL_BY_ID");
    Map<UUID, OrderableFulfillDto> orderableFulfillMap = orderableFulfillService.findByIds(
        approvedProducts.getIdentifiers());

    profiler.start("FIND_STOCK_CARD_BY_PROGRAM_AND_FACILITY");

    List<UUID> orderableIdsForStockCard = Collections.emptyList();
    Set<UUID> lotCodeIds = Collections.emptySet();
    String lotCode = params.getLotCode();

    if (!StringUtils.isBlank(lotCode)) {
      RequestParameters searchParams = RequestParameters
          .init()
          .set("size", Integer.MAX_VALUE)
          .set("lotCode", lotCode);

      Page<LotDto> lotPage = lotReferenceDataService.getPage(searchParams);

      List<UUID> tradeItemsMatchingLotCode = lotPage.map(LotDto::getTradeItemId)
          .toList();

      lotCodeIds = lotPage.map(LotDto::getId).toSet();

      searchParams = RequestParameters
          .init()
          .set("size", tradeItemsMatchingLotCode.size())
          .set("tradeItemId", tradeItemsMatchingLotCode);

      orderableIdsForStockCard = orderableReferenceDataService.getPage(searchParams)
          .stream()
          .map(OrderableDto::getId)
          .collect(toList());
    }

    // FIXME: Fix page retrieving/calculation,
    //  page size may be wrong when there are orderables matching not only by lot codes
    List<StockCard> stockCards = calculatedStockOnHandService.getStockCardsWithStockOnHand(
            params.getProgramId(), params.getFacilityId(), params.getAsOfDate(),
            orderableIdsForStockCard, lotCodeIds);

    Page<OrderableDto> orderablesPage = approvedProducts.getOrderablesPage();
    StockCardSummaries result = new StockCardSummaries(
        orderablesPage.getContent(), stockCards, orderableFulfillMap,
        params.getAsOfDate(), orderablesPage.getTotalElements());

    profiler.stop().log();
    return result;
  }

  /**
   * Find all stock cards by program id and facility id. No paging, all in one.
   * Used for generating pdf file of all stock cards.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   * @return found stock cards.
   */
  public List<StockCardDto> findStockCards(UUID programId, UUID facilityId) {
    return cardsToDtos(stockCardRepository.findByProgramIdAndFacilityId(programId, facilityId));
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
    Page<StockCard> pageOfCards = stockCardRepository
        .findByProgramIdAndFacilityId(programId, facilityId, pageable);

    List<StockCardDto> cardDtos = cardsToDtos(pageOfCards.getContent());
    return new PageImpl<>(cardDtos, pageable, pageOfCards.getTotalElements());
  }

  /**
   * Create dummy cards for approved products and lots that don't have cards yet.
   *
   * @param programId  programId
   * @param facilityId facilityId
   * @return dummy cards.
   */
  public List<StockCardDto> createDummyStockCards(UUID programId, UUID facilityId) {
    //this will not read the whole table, only the orderable id and lot id
    List<OrderableLotIdentity> existingCardIdentities =
        stockCardRepository.getIdentitiesBy(programId, facilityId);

    LOGGER.info("Calling ref data to get all approved orderables");
    Map<OrderableLotIdentity, OrderableLot> orderableLotsMap = createOrderableLots(
        orderableReferenceDataService.findAll());

    //create dummy(fake/not persisted) cards for approved orderables that don't have cards yet
    List<StockCard> dummyCards = createDummyCards(programId, facilityId, orderableLotsMap.values(),
        existingCardIdentities).collect(toList());
    return assignOrderableLotRemoveLineItems(createDtos(dummyCards), orderableLotsMap);
  }

  private List<StockCardDto> cardsToDtos(List<StockCard> cards) {
    LOGGER.info("Calling ref data to get all approved orderables");
    Map<OrderableLotIdentity, OrderableLot> orderableLotsMap = createOrderableLots(
        orderableReferenceDataService.findAll());

    return assignOrderableLotRemoveLineItems(createDtos(cards), orderableLotsMap);
  }

  private List<StockCardDto> assignOrderableLotRemoveLineItems(
      List<StockCardDto> stockCardDtos,
      Map<OrderableLotIdentity, OrderableLot> orderableLotsMap) {
    stockCardDtos.forEach(stockCardDto -> {
      OrderableLot orderableLot = orderableLotsMap.get(identityOf(stockCardDto));
      stockCardDto.setOrderable(orderableLot.getOrderable());
      stockCardDto.setLot(orderableLot.getLot());
      stockCardDto.setLineItems(null);//line items are not needed in summary
    });
    return stockCardDtos;
  }

  private Stream<StockCard> createDummyCards(UUID programId, UUID facilityId,
                                             Collection<OrderableLot> orderableLots,
                                             List<OrderableLotIdentity> cardIdentities) {
    return filterOrderableLotsWithoutCards(orderableLots, cardIdentities)
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
      Collection<OrderableLot> orderableLots, List<OrderableLotIdentity> cardIdentities) {
    return orderableLots.stream()
        .filter(orderableLot -> cardIdentities.stream()
            .noneMatch(cardIdentity -> cardIdentity.equals(identityOf(orderableLot))))
        .collect(toList());
  }

  private Map<OrderableLotIdentity, OrderableLot> createOrderableLots(
      List<OrderableDto> orderableDtos) {
    Stream<OrderableLot> orderableLots = orderableDtos.stream().flatMap(this::lotsOfOrderable);

    Stream<OrderableLot> orderablesOnly = orderableDtos.stream()
        .map(orderableDto -> new OrderableLot(orderableDto, null));

    return concat(orderableLots, orderablesOnly)
        .collect(toMap(OrderableLotIdentity::identityOf, orderableLot -> orderableLot));
  }

  private Stream<OrderableLot> lotsOfOrderable(OrderableDto orderableDto) {
    String tradeItemId = orderableDto.getIdentifiers().get("tradeItem");
    if (tradeItemId != null) {
      return lotReferenceDataService.getAllLotsOf(UUID.fromString(tradeItemId)).stream()
          .map(lot -> new OrderableLot(orderableDto, lot));
    } else {
      return empty();
    }
  }

  private ImmutablePair<UUID, StockCardAggregate> assignOrderableToStockCard(
      StockCard stockCard,
      Map<UUID, OrderableFulfillDto> orderableFulfillMap,
      Set<UUID> orderableIds,
      LocalDate startDate,
      LocalDate endDate) {

    OrderableFulfillDto fulfills = orderableFulfillMap.get(stockCard.getOrderableId());

    UUID orderableId = null == fulfills || isEmpty(fulfills.getCanBeFulfilledByMe())
        ? stockCard.getOrderableId()
        : fulfills.getCanBeFulfilledByMe().get(0);

    List<StockCard> stockCards = new ArrayList<>();
    stockCards.add(stockCard);

    Set<UUID> stockCardIds = stockCards.stream()
        .map(StockCard::getId)
        .collect(toSet());

    List<CalculatedStockOnHand> calculatedStockOnHands;
    if (null == startDate) {
      calculatedStockOnHands = calculatedStockOnHandRepository
          .findByStockCardIdInAndOccurredDateLessThanEqual(stockCardIds, endDate);
      stockCardIds.forEach(stockCardId -> {
        Optional<CalculatedStockOnHand> calculatedStockOnHand = calculatedStockOnHandRepository
            .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
                stockCardId, endDate);
        calculatedStockOnHand.ifPresent(calculatedStockOnHands::add);
      });
    } else {
      calculatedStockOnHands = calculatedStockOnHandRepository
          .findByStockCardIdInAndOccurredDateBetween(stockCardIds, startDate, endDate);
      stockCardIds.forEach(stockCardId -> {
        Optional<CalculatedStockOnHand> calculatedStockOnHand = calculatedStockOnHandRepository
            .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
                stockCardId, startDate);
        calculatedStockOnHand.ifPresent(calculatedStockOnHands::add);
      });
    }

    return new ImmutablePair<>(
        !isEmpty(orderableIds) && !orderableIds.contains(orderableId)
            ? null
            : orderableId,
        new StockCardAggregate(stockCards, calculatedStockOnHands));
  }

  @Getter
  private static class OrderableLot implements IdentifiableByOrderableLot {
    private OrderableDto orderable;
    private LotDto lot;

    OrderableLot(OrderableDto orderable, LotDto lot) {
      this.orderable = orderable;
      this.lot = lot;
    }

    public UUID getLotId() {
      return lot == null ? null : lot.getId();
    }

    public UUID getOrderableId() {
      return orderable.getId();
    }
  }

}
