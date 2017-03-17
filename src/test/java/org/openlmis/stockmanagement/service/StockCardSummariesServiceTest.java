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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramOrderableDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesServiceTest {

  @Mock
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableRefDataService;

  @Mock
  @SuppressWarnings("PMD")
  private FacilityReferenceDataService facilityRefDataService;

  @Mock
  @SuppressWarnings("PMD")
  private ProgramReferenceDataService programRefDataService;

  @Mock
  private StockCardRepository stockCardRepository;

  @InjectMocks
  private StockCardSummariesService stockCardSummariesService;

  @Test
  public void should_contain_existing_stock_cards() throws Exception {
    //given
    UUID orderable1Id = UUID.randomUUID();
    UUID orderable2Id = UUID.randomUUID();
    UUID orderable3Id = UUID.randomUUID();
    UUID orderable4Id = UUID.randomUUID();

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(approvedProductReferenceDataService
        .getAllApprovedProducts(programId, facilityId))
        .thenReturn(asList(
            createApprovedProductDto(orderable1Id),
            createApprovedProductDto(orderable2Id),
            createApprovedProductDto(orderable3Id),
            createApprovedProductDto(orderable4Id)));

    when(stockCardRepository.findByProgramIdAndFacilityId(programId, facilityId)).thenReturn(asList(
        createStockCard(orderable1Id, UUID.randomUUID()),
        createStockCard(orderable3Id, UUID.randomUUID())));

    OrderableDto orderable1 = createOrderable(orderable1Id);
    OrderableDto orderable2 = createOrderable(orderable2Id);
    OrderableDto orderable3 = createOrderable(orderable3Id);
    OrderableDto orderable4 = createOrderable(orderable4Id);

    when(orderableRefDataService.findOne(orderable1Id)).thenReturn(orderable1);
    when(orderableRefDataService.findOne(orderable2Id)).thenReturn(orderable2);
    when(orderableRefDataService.findOne(orderable3Id)).thenReturn(orderable3);
    when(orderableRefDataService.findOne(orderable4Id)).thenReturn(orderable4);

    //when
    List<StockCardDto> cardDtos = stockCardSummariesService.findStockCards(programId, facilityId);

    //then
    assertThat(cardDtos.size(), is(4));

    String orderablePropertyName = "orderable";
    String idPropertyName = "id";
    String lineItemsPropertyName = "lineItems";
    String stockOnHandPropertyName = "stockOnHand";
    String lastUpdatePropertyName = "lastUpdate";

    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable1)),
        hasProperty(idPropertyName, notNullValue()),
        hasProperty(stockOnHandPropertyName, notNullValue()),
        hasProperty(lastUpdatePropertyName, is(of(2017, 3, 18, 15, 10, 31, 100, UTC))),
        hasProperty(lineItemsPropertyName, nullValue()))));
    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable3)),
        hasProperty(idPropertyName, notNullValue()),
        hasProperty(stockOnHandPropertyName, notNullValue()),
        hasProperty(lastUpdatePropertyName, is(of(2017, 3, 18, 15, 10, 31, 100, UTC))),
        hasProperty(lineItemsPropertyName, nullValue()))));

    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable2)),
        hasProperty(idPropertyName, nullValue()),
        hasProperty(stockOnHandPropertyName, nullValue()),
        hasProperty(lastUpdatePropertyName, nullValue()),
        hasProperty(lineItemsPropertyName, nullValue()))));
    assertThat(cardDtos, hasItem(allOf(
        hasProperty(orderablePropertyName, is(orderable4)),
        hasProperty(idPropertyName, nullValue()),
        hasProperty(stockOnHandPropertyName, nullValue()),
        hasProperty(lastUpdatePropertyName, nullValue()),
        hasProperty(lineItemsPropertyName, nullValue()))));
  }

  private OrderableDto createOrderable(UUID orderableId) {
    OrderableDto orderableDto = new OrderableDto();
    orderableDto.setId(orderableId);
    return orderableDto;
  }

  private StockCard createStockCard(UUID orderableId, UUID cardId) {
    StockCard stockCard = new StockCard();
    stockCard.setOrderableId(orderableId);
    stockCard.setId(cardId);
    StockCardLineItem lineItem1 = StockCardLineItem.builder()
        .occurredDate(of(2017, 3, 17, 15, 10, 31, 100, UTC))
        .processedDate(of(2017, 3, 17, 15, 10, 31, 100, UTC))
        .quantity(1)
        .build();
    StockCardLineItem lineItem2 = StockCardLineItem.builder()
        .occurredDate(of(2017, 3, 18, 15, 10, 31, 100, UTC))
        .processedDate(of(2017, 3, 18, 15, 10, 31, 100, UTC))
        .quantity(1)
        .build();
    stockCard.setLineItems(asList(lineItem1, lineItem2, lineItem1));
    return stockCard;
  }

  private ApprovedProductDto createApprovedProductDto(UUID orderableId) {
    ProgramOrderableDto programOrderable = new ProgramOrderableDto();
    programOrderable.setOrderableId(orderableId);
    ApprovedProductDto approvedProductDto = new ApprovedProductDto();
    approvedProductDto.setProgramOrderable(programOrderable);
    return approvedProductDto;
  }
}