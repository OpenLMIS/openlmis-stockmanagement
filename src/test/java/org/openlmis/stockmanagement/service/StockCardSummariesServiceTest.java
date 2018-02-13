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

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableFulfillReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableFulfillDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardSummariesV2SearchParamsDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesServiceTest {

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
  private StockCardRepository cardRepository;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private StockCardSummariesService stockCardSummariesService;

  @Test
  public void shouldCreateDummyCards()
      throws Exception {
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
  public void shouldFindExistingStockCards()
      throws Exception {
    //given
    UUID orderable1Id = randomUUID();
    UUID orderable2Id = randomUUID();
    UUID orderable3Id = randomUUID();
    UUID orderable4Id = randomUUID();

    OrderableDto orderable1 = createOrderableDto(orderable1Id, "");
    OrderableDto orderable2 = createOrderableDto(orderable2Id, "");
    OrderableDto orderable3 = createOrderableDto(orderable3Id, "");
    OrderableDto orderable4 = createOrderableDto(orderable4Id, "");

    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    when(orderableReferenceDataService
        .findAll())
        .thenReturn(asList(orderable1, orderable2, orderable3, orderable4));

    when(cardRepository.findByProgramIdAndFacilityId(programId, facilityId))
        .thenReturn(asList(
            createStockCard(orderable1Id, randomUUID()),
            createStockCard(orderable3Id, randomUUID())));

    when(lotReferenceDataService.getAllLotsOf(any(UUID.class)))
        .thenReturn(emptyList());

    //when
    List<StockCardDto> cardDtos = stockCardSummariesService
        .findStockCards(programId, facilityId);

    //then
    assertThat(cardDtos.size(), is(2));

    String orderablePropertyName = "orderable";
    String idPropertyName = "id";
    String lineItemsPropertyName = "lineItems";
    String stockOnHandPropertyName = "stockOnHand";
    String lastUpdatePropertyName = "lastUpdate";

    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable1)),
        hasProperty(idPropertyName, notNullValue()),
        hasProperty(stockOnHandPropertyName, notNullValue()),
        hasProperty(lastUpdatePropertyName, is(LocalDate.of(2017, 3, 18))),
        hasProperty(lineItemsPropertyName, nullValue()))));
    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable3)),
        hasProperty(idPropertyName, notNullValue()),
        hasProperty(stockOnHandPropertyName, notNullValue()),
        hasProperty(lastUpdatePropertyName, is(LocalDate.of(2017, 3, 18))),
        hasProperty(lineItemsPropertyName, nullValue()))));
  }

  @Test
  public void shouldReturnPageOfStockCards() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    PageRequest pageRequest = new PageRequest(0, 1);

    UUID orderableId = randomUUID();
    OrderableDto orderable = createOrderableDto(orderableId, "");

    StockCard card = createStockCard(orderableId, randomUUID());
    when(cardRepository.findByProgramIdAndFacilityId(programId, facilityId, pageRequest))
        .thenReturn(new PageImpl<>(singletonList(card), pageRequest, 10));

    when(orderableReferenceDataService.findAll())
        .thenReturn(singletonList(orderable));

    when(lotReferenceDataService.getAllLotsOf(any(UUID.class)))
        .thenReturn(emptyList());

    //when
    Page<StockCardDto> stockCards = stockCardSummariesService
        .findStockCards(programId, facilityId, pageRequest);

    //then
    assertThat(stockCards.getContent().size(), is(1));
    assertThat(stockCards.getTotalElements(), is(10L));
    assertThat(stockCards.getContent().get(0).getExtraData().get("vvmStatus"), is("STAGE_2"));
  }

  @Test
  public void shouldFindStockCards() throws Exception {

    OrderableDto orderable = new OrderableDtoDataBuilder().build();
    OrderableDto orderable2 = new OrderableDtoDataBuilder().build();
    OrderableDto orderable3 = new OrderableDtoDataBuilder().build();

    StockCardSummariesV2SearchParams params = new StockCardSummariesV2SearchParamsDataBuilder()
        .withOrderableIds(asList(orderable.getId(), orderable2.getId()))
        .build();

    when(approvedProductReferenceDataService
        .getApprovedProducts(eq(params.getFacilityId()), eq(params.getProgramId()),
            eq(params.getOrderableIds()), eq(params.getPageable())))
        .thenReturn(
            new PageImpl<>(asList(orderable, orderable2, orderable3), params.getPageable(), 3));

    Map<UUID, OrderableFulfillDto> fulfillMap = new HashMap<>();
    fulfillMap.put(orderable.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2.getId(), orderable3.getId())).build());
    fulfillMap.put(orderable2.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable.getId(), orderable3.getId())).build());

    when(orderableFulfillReferenceDataService
        .findByIds(asList(orderable.getId(), orderable2.getId(), orderable3.getId())))
        .thenReturn(fulfillMap);

    StockEvent event = new StockEventDataBuilder()
        .withFacility(params.getFacilityId())
        .withProgram(params.getProgramId())
        .build();

    StockCard stockCard = new StockCardDataBuilder(event)
        .withOrderable(orderable.getId())
        .withStockOnHand(12)
        .build();

    StockCard stockCard1 = new StockCardDataBuilder(event)
        .withOrderable(orderable3.getId())
        .withStockOnHand(26)
        .build();

    List<StockCard> stockCards = asList(stockCard, stockCard1);

    when(cardRepository.findByProgramIdAndFacilityId(
        params.getProgramId(),
        params.getFacilityId()))
        .thenReturn(stockCards);

    StockCardSummaries result = stockCardSummariesService.findStockCards(params);

    assertEquals(3, result.getPageOfApprovedProducts().size());
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldThrowExceptionIfNoPermission() {
    StockCardSummariesV2SearchParams params =
        new StockCardSummariesV2SearchParamsDataBuilder().build();

    doThrow(new
        PermissionMessageException(new Message("no permission")))
        .when(permissionService)
        .canViewStockCard(params.getProgramId(), params.getFacilityId());

    stockCardSummariesService.findStockCards(params);
  }

  private StockCard createStockCard(UUID orderableId, UUID cardId) {
    StockCard stockCard = new StockCard();
    stockCard.setOrderableId(orderableId);
    stockCard.setId(cardId);
    Map<String, String> oldExtraData = new HashMap<>();
    oldExtraData.put("vvmStatus", "STAGE_1");
    Map<String, String> newExtraData = new HashMap<>();
    newExtraData.put("vvmStatus", "STAGE_2");
    StockCardLineItem lineItem1 = StockCardLineItem.builder()
        .occurredDate(LocalDate.of(2017, 3, 17))
        .processedDate(of(2017, 3, 17, 15, 10, 31, 100, UTC))
        .quantity(1)
        .extraData(oldExtraData)
        .build();
    StockCardLineItem lineItem2 = StockCardLineItem.builder()
        .occurredDate(LocalDate.of(2017, 3, 18))
        .processedDate(of(2017, 3, 18, 15, 10, 31, 100, UTC))
        .quantity(1)
        .extraData(newExtraData)
        .build();
    stockCard.setLineItems(asList(lineItem1, lineItem2, lineItem1));
    return stockCard;
  }

  private OrderableDto createOrderableDto(UUID orderableId, String productName) {
    return OrderableDto.builder()
        .id(orderableId)
        .fullProductName(productName)
        .identifiers(new HashMap<>())
        .build();
  }
}