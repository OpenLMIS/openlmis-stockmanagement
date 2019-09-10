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
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardSummariesServiceIntegrationTest extends BaseIntegrationTest {


  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @MockBean
  private LotReferenceDataService lotReferenceDataService;

  @MockBean
  private FacilityReferenceDataService facilityReferenceDataService;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private StockCardRepository cardRepository;

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;
  
  @Autowired
  private StockEventsRepository stockEventsRepository;

  private UUID programId = randomUUID();
  private UUID facilityId = randomUUID();

  @Before
  public void setUp() throws Exception {
    mockAuthentication();
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

    when(orderableReferenceDataService
        .findAll())
        .thenReturn(asList(orderable1, orderable2, orderable3, orderable4));
    when(lotReferenceDataService.getAllLotsOf(any(UUID.class)))
        .thenReturn(emptyList());
    when(facilityReferenceDataService.findOne(any(UUID.class)))
        .thenReturn(new FacilityDto());
    when(programReferenceDataService.findOne(any(UUID.class)))
        .thenReturn(new ProgramDto());

    createStockCard(orderable1Id, randomUUID());
    createStockCard(orderable3Id, randomUUID());
    
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
    UUID orderableId = randomUUID();
    OrderableDto orderable = createOrderableDto(orderableId, "");

    createStockCard(orderableId, randomUUID());

    doReturn(singletonList(orderable)).when(orderableReferenceDataService).findAll();

    doReturn(emptyList()).when(lotReferenceDataService).getAllLotsOf(any(UUID.class));

    PageRequest pageRequest = new PageRequest(0, 1);
    //when
    Page<StockCardDto> stockCards = stockCardSummariesService
        .findStockCards(programId, facilityId, pageRequest);

    //then
    assertThat(stockCards.getContent().size(), is(1));
    assertThat(stockCards.getContent().get(0).getExtraData().get("vvmStatus"), is("STAGE_2"));
  }

  private OrderableDto createOrderableDto(UUID orderableId, String productName) {
    return OrderableDto.builder()
        .id(orderableId)
        .fullProductName(productName)
        .identifiers(new HashMap<>())
        .build();
  }

  private StockCard createStockCard(UUID orderableId, UUID cardId) {
    StockEvent stockEvent = stockEventsRepository.save(new StockEventDataBuilder()
        .withoutId().build());
    
    StockCard stockCard = new StockCard();
    stockCard.setOrderableId(orderableId);
    stockCard.setFacilityId(facilityId);
    stockCard.setProgramId(programId);
    stockCard.setOriginEvent(stockEvent);
    stockCard.setId(cardId);
    Map<String, String> oldExtraData = new HashMap<>();
    oldExtraData.put("vvmStatus", "STAGE_1");
    Map<String, String> newExtraData = new HashMap<>();
    newExtraData.put("vvmStatus", "STAGE_2");
    
    StockCardLineItem lineItem1 = StockCardLineItem.builder()
        .occurredDate(LocalDate.of(2017, 3, 17))
        .processedDate(of(2017, 3, 17, 15, 10, 31, 100, UTC))
        .quantity(1)
        .stockCard(stockCard)
        .userId(randomUUID())
        .originEvent(stockEvent)
        .extraData(oldExtraData)
        .build();
    
    StockCardLineItem lineItem2 = StockCardLineItem.builder()
        .occurredDate(LocalDate.of(2017, 3, 18))
        .processedDate(of(2017, 3, 18, 15, 10, 31, 100, UTC))
        .quantity(1)
        .stockCard(stockCard)
        .userId(randomUUID())
        .originEvent(stockEvent)
        .extraData(newExtraData)
        .build();
    
    stockCard.setLineItems(asList(lineItem1, lineItem2, lineItem1));
    cardRepository.save(stockCard);
    calculatedStockOnHandRepository
        .save(new CalculatedStockOnHand(1, stockCard, 
            LocalDate.of(2017, 3, 17), 
            of(2017, 3, 17, 15, 10, 31, 100, UTC)));
    calculatedStockOnHandRepository
        .save(new CalculatedStockOnHand(1, stockCard, 
            LocalDate.of(2017, 3, 18),
            of(2017, 3, 18, 15, 10, 31, 100, UTC)));
    return stockCard;
  }
}
