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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.referencedata.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderablesAggregator;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableFulfillReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableFulfillDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardSummariesV2SearchParamsDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesServiceTest {

  private final UUID facilityId = randomUUID();
  private final UUID programId = randomUUID();
  private final UUID orderableId1 = randomUUID();
  private final UUID orderableId2 = randomUUID();
  private final UUID orderableId3 = randomUUID();
  private final UUID orderableId4 = randomUUID();
  private final UUID orderableId5 = randomUUID();
  private final UUID orderableId6 = randomUUID();
  private final UUID orderableId7 = randomUUID();
  private final UUID lotId1 = randomUUID();
  private final UUID lotId2 = randomUUID();

  @Mock
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;
  @Mock
  private OrderableFulfillReferenceDataService orderableFulfillReferenceDataService;
  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;
  @Mock
  @SuppressWarnings("PMD")
  private FacilityReferenceDataService facilityRefDataService;
  @Mock
  @SuppressWarnings("PMD")
  private ProgramReferenceDataService programRefDataService;
  @Mock
  private LotReferenceDataService lotReferenceDataService;
  @Mock
  private CalculatedStockOnHandService calculatedStockOnHandService;
  @Mock
  private StockCardRepository cardRepository;
  @Mock
  private PermissionService permissionService;
  @Mock
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;
  @Mock
  private HomeFacilityPermissionService homeFacilityPermissionService;
  @InjectMocks
  private StockCardSummariesService stockCardSummariesService;

  @Mock
  private SecurityContext securityContext;
  @Mock
  private OAuth2Authentication authentication;

  @Before
  public void setUp() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(false);
  }

  @Test
  public void shouldCreateDummyCards() {
    //given
    UUID orderable1Id = randomUUID();
    UUID orderable2Id = randomUUID();
    UUID orderable3Id = randomUUID();
    UUID orderable4Id = randomUUID();

    OrderableDto orderable1 = createOrderableDto(orderable1Id, "1");
    OrderableDto orderable2 = createOrderableDto(orderable2Id, "2");
    orderable2.getIdentifiers().put("tradeItem", randomUUID().toString());
    OrderableDto orderable3 = createOrderableDto(orderable3Id, "3");
    OrderableDto orderable4 = createOrderableDto(orderable4Id, "4");

    UUID programId = randomUUID();
    UUID facilityId = randomUUID();

    //1,2,3,4 all approved
    when(orderableReferenceDataService
        .findAll())
        .thenReturn(asList(orderable1, orderable2, orderable3, orderable4));

    //but only 1, 3 have cards. 2, 4 don't have cards.
    when(cardRepository.getIdentitiesBy(programId, facilityId))
        .thenReturn(asList(
            new OrderableLotIdentity(orderable1Id, null),
            new OrderableLotIdentity(orderable3Id, null)));

    LotDto lotDto = new LotDto();
    lotDto.setId(randomUUID());
    //2 has a lot
    when(lotReferenceDataService
        .getAllLotsOf(fromString(orderable2.getIdentifiers().get("tradeItem"))))
        .thenReturn(singletonList(lotDto));

    //when
    List<StockCardDto> cardDtos = stockCardSummariesService
        .createDummyStockCards(programId, facilityId);

    //then
    assertThat(cardDtos.size(), is(3));

    String orderablePropertyName = "orderable";
    String lotPropertyName = "lot";
    String idPropertyName = "id";
    String lineItemsPropertyName = "lineItems";
    String stockOnHandPropertyName = "stockOnHand";
    String lastUpdatePropertyName = "lastUpdate";

    //2 and lot
    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable2)),
        hasProperty(lotPropertyName, is(lotDto)),
        hasProperty(idPropertyName, nullValue()),
        hasProperty(stockOnHandPropertyName, nullValue()),
        hasProperty(lineItemsPropertyName, nullValue()))));

    //2 and no lot
    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable2)),
        hasProperty(lotPropertyName, is(nullValue())),
        hasProperty(idPropertyName, nullValue()),
        hasProperty(stockOnHandPropertyName, nullValue()),
        hasProperty(lastUpdatePropertyName, nullValue()),
        hasProperty(lineItemsPropertyName, nullValue()))));

    //4 and no lot
    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable4)),
        hasProperty(lotPropertyName, is(nullValue())),
        hasProperty(idPropertyName, nullValue()),
        hasProperty(stockOnHandPropertyName, nullValue()),
        hasProperty(lastUpdatePropertyName, nullValue()),
        hasProperty(lineItemsPropertyName, nullValue()))));
  }

  @Test
  public void shouldFindStockCards() {
    ProgramDto program =
        ProgramDto.builder().id(UUID.fromString("6af5c325-990d-4af0-af46-699ebe3dc38a")).build();
    OrderableDto orderable = new OrderableDtoDataBuilder().build();
    OrderableDto orderable2 = new OrderableDtoDataBuilder().build();
    OrderableDto orderable3 = new OrderableDtoDataBuilder().build();

    OrderablesAggregator orderablesAggregator = new OrderablesAggregator(asList(
        new ApprovedProductDto(orderable, program, null),
        new ApprovedProductDto(orderable2, program, null),
        new ApprovedProductDto(orderable3, program, null)
    ));

    StockCardSummariesV2SearchParams params = new StockCardSummariesV2SearchParamsDataBuilder()
        .withOrderableIds(asList(orderable.getId(), orderable2.getId()))
        .build();

    when(approvedProductReferenceDataService
        .getApprovedProducts(
            eq(params.getFacilityId()),
            eq(params.getProgramIds()),
            eq(params.getOrderableIds()),
            eq(params.getOrderableCode()),
            eq(params.getOrderableName())
        ))
        .thenReturn(orderablesAggregator);

    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderable.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2.getId(), orderable3.getId())).build());
    fulfillMap.put(orderable2.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable.getId(), orderable3.getId())).build());

    when(orderableFulfillReferenceDataService
        .findByIds(asList(orderable.getId(), orderable2.getId(), orderable3.getId())))
        .thenReturn(fulfillMap);

    when(lotReferenceDataService.getPage(any(RequestParameters.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(orderableReferenceDataService.getPage(any(RequestParameters.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(homeFacilityPermissionService.checkFacilityAndHomeFacilityLinkage(any(UUID.class)))
        .thenReturn(false);

    StockEvent event = new StockEventDataBuilder()
        .withFacility(params.getFacilityId())
        .withProgram(params.getProgramIds().get(0))
        .build();

    StockCard stockCard = new StockCardDataBuilder(event)
        .withOrderableId(orderable.getId())
        .withStockOnHand(12)
        .build();

    StockCard stockCard1 = new StockCardDataBuilder(event)
        .withOrderableId(orderable3.getId())
        .withStockOnHand(26)
        .build();

    List<StockCard> stockCards = asList(stockCard, stockCard1);

    when(calculatedStockOnHandService
        .getStockCardsWithStockOnHand(params.getProgramIds(), params.getFacilityId(),
            params.getAsOfDate(), Collections.emptyList(), Collections.emptySet()))
        .thenReturn(stockCards);

    StockCardSummaries result = stockCardSummariesService.findStockCards(params);

    assertEquals(3, result.getPageOfApprovedProducts().size());
  }

  @Test
  public void shouldNotCallPermissionServiceWhenApplicationClientOnly() {
    OrderableDto orderable = new OrderableDtoDataBuilder().build();
    OrderableDto orderable2 = new OrderableDtoDataBuilder().build();

    OrderablesAggregator orderablesAggregator = new OrderablesAggregator(asList(
        new ApprovedProductDto(orderable, null, null),
        new ApprovedProductDto(orderable2, null, null)
    ));

    StockCardSummariesV2SearchParams params = new StockCardSummariesV2SearchParamsDataBuilder()
        .withOrderableIds(asList(orderable.getId(), orderable2.getId()))
        .build();

    when(approvedProductReferenceDataService
        .getApprovedProducts(
            eq(params.getFacilityId()),
            eq(params.getProgramIds()),
            eq(params.getOrderableIds()),
            eq(params.getOrderableCode()),
            eq(params.getOrderableName())
        ))
        .thenReturn(orderablesAggregator);

    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderable.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(singletonList(orderable2.getId())).build());
    fulfillMap.put(orderable2.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(singletonList(orderable.getId())).build());

    when(orderableFulfillReferenceDataService
        .findByIds(asList(orderable.getId(), orderable2.getId())))
        .thenReturn(fulfillMap);

    when(lotReferenceDataService.getPage(any(RequestParameters.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(orderableReferenceDataService.getPage(any(RequestParameters.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(authentication.isClientOnly()).thenReturn(true);

    StockEvent event = new StockEventDataBuilder()
        .withFacility(params.getFacilityId())
        .withProgram(params.getProgramIds().get(0))
        .build();

    StockCard stockCard = new StockCardDataBuilder(event)
        .withOrderableId(orderable.getId())
        .withStockOnHand(12)
        .build();

    List<StockCard> stockCards = singletonList(stockCard);

    when(calculatedStockOnHandService
        .getStockCardsWithStockOnHand(params.getProgramIds(), params.getFacilityId(),
            params.getAsOfDate(), Collections.emptyList(), Collections.emptySet()))
        .thenReturn(stockCards);

    StockCardSummaries result = stockCardSummariesService.findStockCards(params);

    assertEquals(2, result.getPageOfApprovedProducts().size());
    verify(permissionService, never())
        .canViewStockCard(any(UUID.class), any(UUID.class));
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldThrowExceptionIfNoPermission() {
    ProgramDto program =
        ProgramDto.builder().id(UUID.fromString("6af5c325-990d-4af0-af46-699ebe3dc38b")).build();
    OrderableDto orderable = new OrderableDtoDataBuilder().build();
    OrderableDto orderable2 = new OrderableDtoDataBuilder().build();
    OrderableDto orderable3 = new OrderableDtoDataBuilder().build();

    OrderablesAggregator orderablesAggregator = new OrderablesAggregator(asList(
        new ApprovedProductDto(orderable, program, null),
        new ApprovedProductDto(orderable2, program, null),
        new ApprovedProductDto(orderable3, program, null)
    ));

    StockCardSummariesV2SearchParams params =
        new StockCardSummariesV2SearchParamsDataBuilder().build();

    when(approvedProductReferenceDataService
        .getApprovedProducts(
            eq(params.getFacilityId()),
            eq(params.getProgramIds()),
            eq(params.getOrderableIds()),
            eq(params.getOrderableCode()),
            eq(params.getOrderableName())
        ))
        .thenReturn(orderablesAggregator);

    when(homeFacilityPermissionService.checkFacilityAndHomeFacilityLinkage(any(UUID.class)))
        .thenReturn(false);

    doThrow(new
        PermissionMessageException(new Message("no permission")))
        .when(permissionService)
        .canViewStockCard(any(UUID.class), any(UUID.class));

    stockCardSummariesService.findStockCards(params);
  }

  @Test
  public void shouldAggregateStockCardsByCommodityTypes() {
    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderableId2, new OrderableFulfillDtoDataBuilder()
        .withCanBeFulfilledByMe(singletonList(orderableId1)).build());
    fulfillMap.put(orderableId3, new OrderableFulfillDtoDataBuilder()
        .withCanBeFulfilledByMe(singletonList(orderableId1)).build());
    fulfillMap.put(orderableId5, new OrderableFulfillDtoDataBuilder()
        .withCanBeFulfilledByMe(singletonList(orderableId4)).build());
    fulfillMap.put(orderableId7, new OrderableFulfillDtoDataBuilder().build());

    when(orderableFulfillReferenceDataService
        .findByIds(
            ImmutableSet.of(orderableId2, orderableId3,
                orderableId5, orderableId6, orderableId7)))
        .thenReturn(fulfillMap);

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();

    StockCard stockCard1 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId2)
        .withStockOnHand(12)
        .build();

    StockCard stockCard2 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId3)
        .withStockOnHand(26)
        .build();

    StockCard stockCard3 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId5)
        .withStockOnHand(36)
        .build();

    StockCard stockCard4 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId6)
        .withStockOnHand(46)
        .build();

    StockCard stockCard5 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId7)
        .withStockOnHand(56)
        .build();

    when(calculatedStockOnHandService
        .getStockCardsWithStockOnHand(programId, facilityId))
        .thenReturn(asList(stockCard1, stockCard2, stockCard3, stockCard4, stockCard5));

    List<CalculatedStockOnHand> calculatedStockOnHands = new ArrayList<>();
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHandDataBuilder().build();
    calculatedStockOnHands.add(calculatedStockOnHand);
    when(calculatedStockOnHandRepository
        .findByStockCardIdInAndOccurredDateBetween(any(), any(), any()))
        .thenReturn(calculatedStockOnHands);

    final CalculatedStockOnHand calculatedStockOnHand1 =
        generateAndMockCalculatedStockOnHand(stockCard1);
    final CalculatedStockOnHand calculatedStockOnHand2 =
        generateAndMockCalculatedStockOnHand(stockCard2);
    final CalculatedStockOnHand calculatedStockOnHand3 =
        generateAndMockCalculatedStockOnHand(stockCard3);
    final CalculatedStockOnHand calculatedStockOnHand4 =
        generateAndMockCalculatedStockOnHand(stockCard4);
    final CalculatedStockOnHand calculatedStockOnHand5 =
        generateAndMockCalculatedStockOnHand(stockCard5);

    Map<UUID, StockCardAggregate> cardMap =
        stockCardSummariesService.getGroupedStockCards(programId, facilityId, null,
            LocalDate.of(2017, 3, 16),
            LocalDate.of(2017, 3, 19));

    assertThat(cardMap.keySet(), hasItems(orderableId1, orderableId4,
        orderableId6, orderableId7));

    assertThat(cardMap.get(orderableId1).getStockCards(), hasItems(stockCard1, stockCard2));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand1, calculatedStockOnHand2));

    assertThat(cardMap.get(orderableId4).getStockCards(), hasItems(stockCard3));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand3));

    assertThat(cardMap.get(orderableId6).getStockCards(), hasItems(stockCard4));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand4));

    assertThat(cardMap.get(orderableId7).getStockCards(), hasItems(stockCard5));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand5));
  }

  @Test
  public void shouldAggregateStockCardsByCommodityTypesWhenNoStartDateProvided() {
    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderableId2, new OrderableFulfillDtoDataBuilder()
        .withCanBeFulfilledByMe(singletonList(orderableId1)).build());
    fulfillMap.put(orderableId3, new OrderableFulfillDtoDataBuilder()
        .withCanBeFulfilledByMe(singletonList(orderableId1)).build());
    fulfillMap.put(orderableId5, new OrderableFulfillDtoDataBuilder()
        .withCanBeFulfilledByMe(singletonList(orderableId4)).build());
    fulfillMap.put(orderableId7, new OrderableFulfillDtoDataBuilder().build());

    when(orderableFulfillReferenceDataService
        .findByIds(
            ImmutableSet.of(orderableId2, orderableId3, orderableId5,
                orderableId6, orderableId7)))
        .thenReturn(fulfillMap);

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();

    StockCard stockCard1 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId2)
        .withStockOnHand(12)
        .build();

    StockCard stockCard2 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId3)
        .withStockOnHand(26)
        .build();

    StockCard stockCard3 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId5)
        .withStockOnHand(36)
        .build();

    StockCard stockCard4 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId6)
        .withStockOnHand(46)
        .build();

    StockCard stockCard5 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId7)
        .withStockOnHand(56)
        .build();

    when(calculatedStockOnHandService
        .getStockCardsWithStockOnHand(programId, facilityId))
        .thenReturn(asList(stockCard1, stockCard2, stockCard3, stockCard4, stockCard5));

    List<CalculatedStockOnHand> calculatedStockOnHands = new ArrayList<>();
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHandDataBuilder().build();
    calculatedStockOnHands.add(calculatedStockOnHand);

    when(calculatedStockOnHandRepository
        .findByStockCardIdInAndOccurredDateLessThanEqual(any(), any()))
        .thenReturn(calculatedStockOnHands);

    final CalculatedStockOnHand calculatedStockOnHand1 =
        generateCalculatedStockOnHandWithEndDate(stockCard1);
    final CalculatedStockOnHand calculatedStockOnHand2 =
        generateCalculatedStockOnHandWithEndDate(stockCard2);
    final CalculatedStockOnHand calculatedStockOnHand3 =
        generateCalculatedStockOnHandWithEndDate(stockCard3);
    final CalculatedStockOnHand calculatedStockOnHand4 =
        generateCalculatedStockOnHandWithEndDate(stockCard4);
    final CalculatedStockOnHand calculatedStockOnHand5 =
        generateCalculatedStockOnHandWithEndDate(stockCard5);

    Map<UUID, StockCardAggregate> cardMap =
        stockCardSummariesService.getGroupedStockCards(programId, facilityId, null,
            null, LocalDate.of(2017, 3, 19));

    assertThat(cardMap.keySet(), hasItems(orderableId1, orderableId4,
        orderableId6, orderableId7));

    assertThat(cardMap.get(orderableId1).getStockCards(), hasItems(stockCard1, stockCard2));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand1, calculatedStockOnHand2));

    assertThat(cardMap.get(orderableId4).getStockCards(), hasItems(stockCard3));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand3));

    assertThat(cardMap.get(orderableId6).getStockCards(), hasItems(stockCard4));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand4));

    assertThat(cardMap.get(orderableId7).getStockCards(), hasItems(stockCard5));
    assertThat(cardMap.get(orderableId1).getCalculatedStockOnHands(),
        hasItems(calculatedStockOnHand, calculatedStockOnHand5));
  }

  @Test
  public void shouldFindStockCardsForProgramAndFacilityIds() {
    //given
    prepareForFindStockCards(null);

    //when
    List<StockCardDto> stockCardsDtos =
        stockCardSummariesService.findStockCards(programId, facilityId);

    //then
    assertThat(stockCardsDtos, hasSize(2));
    checkStockCardDto(stockCardsDtos, orderableId1, lotId1);
    checkStockCardDto(stockCardsDtos, orderableId2, lotId2);
  }

  @Test
  public void shouldFindStockCardsForProgramAndFacilityIdsAndPageableAndProfiler() {
    //given
    Pageable pageable = mock(Pageable.class);
    Profiler profiler = mock(Profiler.class);
    prepareForFindStockCards(pageable);

    //when
    Page<StockCardDto> stockCardsPage =
        stockCardSummariesService.findStockCards(programId, facilityId, pageable, profiler);

    //then
    assertEquals(2L, stockCardsPage.getTotalElements());
    List<StockCardDto> stockCardDtos = stockCardsPage.get().collect(Collectors.toList());
    checkStockCardDto(stockCardDtos, orderableId1, lotId1);
    checkStockCardDto(stockCardDtos, orderableId2, lotId2);
  }

  private void prepareForFindStockCards(Pageable pageable) {
    OrderableDto orderable1 = createOrderableDto(orderableId1, "1");
    OrderableDto orderable2 = createOrderableDto(orderableId2, "2");

    LotDto lot1 = LotDto.builder().id(lotId1).build();
    LotDto lot2 = LotDto.builder().id(lotId2).build();

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();

    StockCard stockCard1 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId1)
        .withStockOnHand(12)
        .withLotId(lotId1)
        .build();

    StockCard stockCard2 = new StockCardDataBuilder(event)
        .withOrderableId(orderableId2)
        .withStockOnHand(26)
        .withLotId(lotId2)
        .build();

    List<StockCard> stockCards = asList(stockCard1, stockCard2);

    if (pageable != null) {
      Page<StockCard> stockCardPage = new PageImpl<>(stockCards, pageable, stockCards.size());
      when(cardRepository.findByProgramIdAndFacilityId(programId, facilityId, pageable))
          .thenReturn(stockCardPage);
    } else {
      when(cardRepository.findByProgramIdAndFacilityId(programId, facilityId))
          .thenReturn(stockCards);
    }

    when(orderableReferenceDataService
        .findByIds(new HashSet<>(Arrays.asList(orderableId1, orderableId2))))
        .thenReturn(Arrays.asList(orderable1, orderable2));
    when(lotReferenceDataService.findByIds(new HashSet<>(Arrays.asList(lotId1, lotId2))))
        .thenReturn(Arrays.asList(lot1, lot2));
  }

  private void checkStockCardDto(
      List<StockCardDto> stockCardsDtos,
      UUID orderableId,
      UUID lotId) {
    Optional<StockCardDto> stockCardDtoOptional = stockCardsDtos.stream()
        .filter(stockCardDto -> stockCardDto.getOrderableId().equals(orderableId))
        .findFirst();
    assertThat(stockCardDtoOptional.isPresent(), is(true));
    StockCardDto stockCardDto = stockCardDtoOptional.get();
    assertThat(stockCardDto.getLotId(), is(lotId));
  }

  private OrderableDto createOrderableDto(UUID orderableId, String productName) {
    return OrderableDto.builder()
        .id(orderableId)
        .fullProductName(productName)
        .identifiers(new HashMap<>())
        .build();
  }

  private CalculatedStockOnHand generateAndMockCalculatedStockOnHand(StockCard stockCard) {
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHandDataBuilder().build();
    when(calculatedStockOnHandRepository
        .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
            stockCard.getId(), LocalDate.of(2017, 3, 16)))
        .thenReturn(Optional.ofNullable(calculatedStockOnHand));
    return calculatedStockOnHand;
  }

  private CalculatedStockOnHand generateCalculatedStockOnHandWithEndDate(StockCard stockCard) {
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHandDataBuilder().build();
    when(calculatedStockOnHandRepository
        .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
            stockCard.getId(), LocalDate.of(2017, 3, 19)))
        .thenReturn(Optional.ofNullable(calculatedStockOnHand));
    return calculatedStockOnHand;
  }

}
