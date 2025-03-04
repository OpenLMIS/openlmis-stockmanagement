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

import static org.slf4j.LoggerFactory.getLogger;
import static org.slf4j.ext.XLoggerFactory.getXLogger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.LazyGrouping;
import org.openlmis.stockmanagement.util.LazyList;
import org.openlmis.stockmanagement.util.LazyResource;
import org.openlmis.stockmanagement.util.ReferenceDataSupplier;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.slf4j.Logger;
import org.slf4j.ext.XLogger;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

/**
 * Before we process a stock event, this class will run first, to get all things we need from
 * reference data. So that network traffic will be concentrated at one place rather than scattered
 * all around the place.
 */
@Service
@NoArgsConstructor
@AllArgsConstructor
public class StockEventProcessContextBuilder {
  private static final Logger LOGGER = getLogger(StockEventProcessContextBuilder.class);
  private static final XLogger XLOGGER = getXLogger(StockEventProcessContextBuilder.class);

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private FacilityReferenceDataService facilityService;

  @Autowired
  private ProgramReferenceDataService programService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private NodeRepository nodeRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private CalculatedStockOnHandService calculatedStockOnHandService;

  @Autowired
  private ValidSourceAssignmentRepository validSourceAssignmentRepository;

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @Value("${stockmanagement.kit.unpack.reasonId}")
  private UUID unpackReasonId;

  /**
   * Before processing events, put all needed ref data into context so we don't have to do frequent
   * network requests.
   *
   * @param eventDto event dto.
   * @return a context object that includes all needed ref data.
   */
  public StockEventProcessContext buildContext(StockEventDto eventDto) {
    XLOGGER.entry(eventDto);
    Profiler profiler = new Profiler("BUILD_CONTEXT");
    profiler.setLogger(XLOGGER);

    LOGGER.info("build stock event process context");
    StockEventProcessContext context = new StockEventProcessContext();

    profiler.start("CREATE_LAZY_USER");
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    Supplier<UUID> userIdSupplier;

    context.setUnpackReasonId(unpackReasonId);

    if (authentication.isClientOnly()) {
      userIdSupplier = eventDto::getUserId;
    } else {
      userIdSupplier = () -> authenticationHelper.getCurrentUser().getId();
    }

    LazyResource<UUID> userId = new LazyResource<>(userIdSupplier);
    context.setCurrentUserId(userId);

    profiler.start("CREATE_LAZY_PROGRAM");
    UUID programId = eventDto.getProgramId();
    Supplier<ProgramDto> programSupplier = new ReferenceDataSupplier<>(
        programService, programId
    );
    LazyResource<ProgramDto> program = new LazyResource<>(programSupplier);
    context.setProgram(program);

    profiler.start("CREATE_LAZY_FACILITY");
    UUID facilityId = eventDto.getFacilityId();
    Supplier<FacilityDto> facilitySupplier = new ReferenceDataSupplier<>(
        facilityService, facilityId
    );
    LazyResource<FacilityDto> facility = new LazyResource<>(facilitySupplier);
    context.setFacility(facility);

    profiler.start("CREATE_LAZY_APPROVED_PRODUCTS");
    List<UUID> orderableIds = eventDto.getLineItems()
        .stream()
        .map(StockEventLineItemDto::getOrderableId)
        .collect(Collectors.toList());
    Supplier<List<OrderableDto>> productsSupplier = () -> orderableReferenceDataService
        .findByIds(orderableIds);
    LazyList<OrderableDto> products = new LazyList<>(productsSupplier);
    context.setAllApprovedProducts(products);

    profiler.start("CREATE_LAZY_LOTS");
    Supplier<List<LotDto>> lotsSupplier = () -> getLots(eventDto);
    LazyList<LotDto> lots = new LazyList<>(lotsSupplier);
    LazyGrouping<UUID, LotDto> lotsGroupedById = new LazyGrouping<>(lots, LotDto::getId);
    context.setLots(lotsGroupedById);

    profiler.start("CREATE_LAZY_EVENT_REASONS");
    Supplier<List<StockCardLineItemReason>> eventReasonsSupplier = () -> reasonRepository
        .findByIdIn(eventDto.getReasonIds());
    LazyList<StockCardLineItemReason> eventReasons = new LazyList<>(eventReasonsSupplier);
    LazyGrouping<UUID, StockCardLineItemReason> eventReasonsGroupedById = new LazyGrouping<>(
        eventReasons, StockCardLineItemReason::getId
    );
    context.setEventReasons(eventReasonsGroupedById);

    profiler.start("CREATE_LAZY_NODES");
    Supplier<List<Node>> nodesSupplier = () -> nodeRepository
        .findByIdIn(eventDto.getNodeIds());
    LazyList<Node> nodes = new LazyList<>(nodesSupplier);
    LazyGrouping<UUID, Node> nodesGroupedById = new LazyGrouping<>(nodes, Node::getId);
    context.setNodes(nodesGroupedById);

    profiler.start("CREATE_LAZY_STOCK_CARDS");
    Supplier<List<StockCard>> cardsSupplier = () -> calculatedStockOnHandService
        .getStockCardsWithStockOnHandByOrderableIds(eventDto.getProgramId(),
            eventDto.getFacilityId(), orderableIds);
    LazyList<StockCard> cards = new LazyList<>(cardsSupplier);
    LazyGrouping<OrderableLotIdentity, StockCard> cardsGroupedByIdentity = new LazyGrouping<>(
        cards, OrderableLotIdentity::identityOf
    );
    context.setCards(cardsGroupedByIdentity);

    profiler.start("CREATE_LAZY_CARD_REASONS");
    Supplier<List<StockCardLineItemReason>> cardReasonsSupplier = () -> getCardReasons(eventDto);
    LazyList<StockCardLineItemReason> cardReasons = new LazyList<>(cardReasonsSupplier);
    LazyGrouping<UUID, StockCardLineItemReason> cardReasonsGroupedById = new LazyGrouping<>(
        cardReasons, StockCardLineItemReason::getId
    );
    context.setCardReasons(cardReasonsGroupedById);

    profiler.start("CREATE_LAZY_SOURCES");
    Supplier<List<ValidSourceAssignment>> sourcesSupplier = () -> validSourceAssignmentRepository
        .findByProgramIdAndFacilityTypeId(
                eventDto.getProgramId(), context.getFacilityTypeId(), Pageable.unpaged());
    LazyList<ValidSourceAssignment> sources = new LazyList<>(sourcesSupplier);
    context.setSources(sources);

    profiler.start("CREATE_LAZY_DESTINATIONS");
    Supplier<List<ValidDestinationAssignment>> destinationsSupplier = () ->
        validDestinationAssignmentRepository
        .findByProgramIdAndFacilityTypeId(
                eventDto.getProgramId(), context.getFacilityTypeId(), Pageable.unpaged());
    LazyList<ValidDestinationAssignment> destinations = new LazyList<>(destinationsSupplier);
    context.setDestinations(destinations);

    profiler.stop().log();
    XLOGGER.exit(context);

    return context;
  }

  private List<LotDto> getLots(StockEventDto eventDto) {
    return eventDto
        .getLineItems()
        .stream()
        .filter(item -> item.getLotId() != null)
        .map(StockEventLineItemDto::getLotId)
        .distinct()
        .map(lotReferenceDataService::findOne)
        .collect(Collectors.toList());
  }

  private List<StockCardLineItemReason> getCardReasons(StockEventDto eventDto) {
    Set<UUID> reasonIds = stockCardRepository
        .findByProgramIdAndFacilityId(eventDto.getProgramId(), eventDto.getFacilityId())
        .stream()
        .map(StockCard::getSortedLineItems)
        .flatMap(Collection::stream)
        .map(StockCardLineItem::getReason)
        .filter(Objects::nonNull)
        .map(StockCardLineItemReason::getId)
        .collect(Collectors.toSet());

    return reasonRepository.findByIdIn(reasonIds);
  }
}
