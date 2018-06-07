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

package org.openlmis.stockmanagement.web.stockcardrangesummary;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.StockCardAggregate;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardRangeSummaryDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.PageImplRepresentation;
import org.openlmis.stockmanagement.web.BaseWebIntegrationTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class StockCardRangeSummaryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String API_STOCK_CARD_RANGE_SUMMARIES = "/api/stockCardRangeSummaries";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String START_DATE = "startDate";
  private static final String END_DATE = "endDate";
  private static final String ORDERABLE_ID = "orderableId";
  private static final String TAG = "tag";

  @MockBean
  private StockCardSummariesService stockCardSummariesService;

  @MockBean
  private StockCardRangeSummaryBuilder builder;

  private Pageable pageable;
  private Page<StockCardRangeSummaryDto> rangeSummaryPage;
  private UUID facilityId = randomUUID();
  private UUID programId = randomUUID();
  private Map<UUID, StockCardAggregate> groupedStockCards;

  @Before
  public void setUp() {
    StockEvent event = new StockEventDataBuilder().build();
    StockCard stockCard = new StockCardDataBuilder(event).build();

    groupedStockCards =
        ImmutableMap.of(randomUUID(), new StockCardAggregate(singletonList(stockCard)));

    doReturn(groupedStockCards)
        .when(stockCardSummariesService).getGroupedStockCards(any(), any(), any());

    pageable = new PageRequest(0, 10);

    rangeSummaryPage = new PageImpl(
        singletonList(new StockCardRangeSummaryDtoDataBuilder()
            .withStockOutDays(10L)
            .build()),
        pageable, 2);

    when(builder.build(any(), any(), any(), any(), any())).thenReturn(rangeSummaryPage);
  }

  @Test
  public void shouldGetStockRangeSummaries() {
    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID, facilityId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(API_STOCK_CARD_RANGE_SUMMARIES)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(PageImplRepresentation.class);

    assertEquals("10",
        ((LinkedHashMap) response.getContent().get(0)).get("stockOutDays").toString());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(stockCardSummariesService).getGroupedStockCards(programId, facilityId, null);
    verify(builder).build(
        groupedStockCards, null, null, LocalDate.now(), new PageRequest(0, Integer.MAX_VALUE));
  }

  @Test
  public void shouldGetStockRangeSummariesByAllParameters() {
    final LocalDate startDate = LocalDate.of(2017, 10, 10);
    final LocalDate endDate = LocalDate.of(2017, 10, 20);
    final String tag = "tag";
    final UUID orderableId1 = randomUUID();
    final UUID orderableId2 = randomUUID();

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID, facilityId)
        .queryParam(PROGRAM_ID, programId)
        .queryParam(START_DATE, "2017-10-10")
        .queryParam(END_DATE, "2017-10-20")
        .queryParam(TAG, tag)
        .queryParam(ORDERABLE_ID, orderableId1)
        .queryParam(ORDERABLE_ID, orderableId2)
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .when()
        .get(API_STOCK_CARD_RANGE_SUMMARIES)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(PageImplRepresentation.class);

    assertEquals("10",
        ((LinkedHashMap) response.getContent().get(0)).get("stockOutDays").toString());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(stockCardSummariesService)
        .getGroupedStockCards(programId, facilityId, ImmutableSet.of(orderableId1, orderableId2));
    verify(builder)
        .build(groupedStockCards, tag, startDate, endDate, pageable);
  }

  @Test
  public void shouldReturnForbiddenIfThereIsNoPermissionForViewingStockCards() {
    doThrow(new PermissionMessageException(new Message("some error")))
        .when(permissionService).canViewStockCard(programId, facilityId);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID, facilityId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(API_STOCK_CARD_RANGE_SUMMARIES)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}