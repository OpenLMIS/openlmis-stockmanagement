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

package org.openlmis.stockmanagement.web.stockcardsummariesv2;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.StockCardSummaries;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams;
import org.openlmis.stockmanagement.testutils.ObjectGenerator;
import org.openlmis.stockmanagement.testutils.StockCardSummariesV2SearchParamsDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardSummaryV2DtoDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.web.BaseWebTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.ResultActions;

public class StockCardSummariesV2ControllerIntegrationTest extends BaseWebTest {

  private static final String API_STOCK_CARD_SUMMARIES = "/api/v2/stockCardSummaries";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String AS_OF_DATE = "asOfDate";
  private static final String ORDERABLE_ID = "orderableId";
  private static final String NON_EMPTY_ONLY = "nonEmptyOnly";
  private static final String PREFIX = "$.";
  private static final String CONTENT_REFERENCE = PREFIX + "content";
  private static final String NUMBER_OF_ELEMENTS_REFERENCE = PREFIX + "numberOfElements";
  private static final String NUMBER_REFERENCE = PREFIX + "number";
  private static final String SIZE_REFERENCE = PREFIX + "size";
  private static final String ORDERABLE_ID_REFERENCE = PREFIX + "content[0].orderable.id";

  @MockBean
  private StockCardSummariesService stockCardSummariesService;

  @MockBean
  private StockCardSummariesV2DtoBuilder stockCardSummariesV2DtoBuilder;

  private StockCardSummaryV2Dto stockCardSummary = new StockCardSummaryV2DtoDataBuilder().build();
  private StockCardSummaryV2Dto stockCardSummary2 = new StockCardSummaryV2DtoDataBuilder().build();
  private StockCardSummariesV2SearchParams params =
      new StockCardSummariesV2SearchParamsDataBuilder().build();
  private StockCardSummaries summaries = ObjectGenerator.of(StockCardSummaries.class);
  private Pageable pageable = new PageRequest(0, 10);

  @Before
  public void setUp() {
    when(stockCardSummariesService
        .findStockCards(any(StockCardSummariesV2SearchParams.class)))
        .thenReturn(summaries);

    when(stockCardSummariesV2DtoBuilder
        .build(summaries.getPageOfApprovedProducts(),
            summaries.getStockCardsForFulfillOrderables(),
            summaries.getOrderableFulfillMap(),
            false))
        .thenReturn(asList(stockCardSummary, stockCardSummary2));
  }

  @Test
  public void shouldGetStockCardSummariesByAllParameters() throws Exception {
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString()));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(CONTENT_REFERENCE, hasSize(2)))
        .andExpect(jsonPath(NUMBER_OF_ELEMENTS_REFERENCE, is(2)))
        .andExpect(jsonPath(NUMBER_REFERENCE, is(pageable.getPageNumber())))
        .andExpect(jsonPath(SIZE_REFERENCE, is(pageable.getPageSize())))
        .andExpect(jsonPath(ORDERABLE_ID_REFERENCE,
            is(stockCardSummary.getOrderable().getId().toString())))
        .andExpect(jsonPath("$.content[1].orderable.id",
            is(stockCardSummary2.getOrderable().getId().toString())));
  }

  @Test
  public void shouldSetIntegerMaxValueAsDefaultPageSize() throws Exception {
    pageable = new PageRequest(0, Integer.MAX_VALUE);

    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString()));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(CONTENT_REFERENCE, hasSize(2)))
        .andExpect(jsonPath(NUMBER_OF_ELEMENTS_REFERENCE, is(2)))
        .andExpect(jsonPath(NUMBER_REFERENCE, is(pageable.getPageNumber())))
        .andExpect(jsonPath(SIZE_REFERENCE, is(pageable.getPageSize())))
        .andExpect(jsonPath(ORDERABLE_ID_REFERENCE,
            is(stockCardSummary.getOrderable().getId().toString())))
        .andExpect(jsonPath("$.content[1].orderable.id",
            is(stockCardSummary2.getOrderable().getId().toString())));
  }

  @Test
  public void shouldReturnBadRequestIfFacilityIdIsNotPresent() throws Exception {
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString()));

    resultActions
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldReturnBadRequestIfProgramIdIsNotPresent() throws Exception {
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(FACILITY_ID, params.getFacilityId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString()));

    resultActions
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldReturnForbiddenIfNoPermission() throws Exception {
    doThrow(new PermissionMessageException(new Message("no permission")))
        .when(stockCardSummariesService)
        .findStockCards(any(StockCardSummariesV2SearchParams.class));

    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString()));

    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void shouldReturnNonEmptySummariesIfFlagIsSet() throws Exception {
    params.setNonEmptyOnly(true);

    when(stockCardSummariesService
        .findStockCards(params))
        .thenReturn(summaries);

    when(stockCardSummariesV2DtoBuilder
        .build(summaries.getPageOfApprovedProducts(),
            summaries.getStockCardsForFulfillOrderables(),
            summaries.getOrderableFulfillMap(),
            true))
        .thenReturn(singletonList(stockCardSummary));

    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString())
            .param(NON_EMPTY_ONLY, Boolean.toString(params.isNonEmptyOnly())));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(CONTENT_REFERENCE, hasSize(1)))
        .andExpect(jsonPath(NUMBER_OF_ELEMENTS_REFERENCE, is(1)))
        .andExpect(jsonPath(NUMBER_REFERENCE, is(pageable.getPageNumber())))
        .andExpect(jsonPath(SIZE_REFERENCE, is(pageable.getPageSize())))
        .andExpect(jsonPath(ORDERABLE_ID_REFERENCE,
            is(stockCardSummary.getOrderable().getId().toString())));
  }

  @Test
  public void shouldRespectSendNonEmptyCardsFlagInSubsequentRequests() throws Exception {
    params.setNonEmptyOnly(true);

    when(stockCardSummariesService
        .findStockCards(params))
        .thenReturn(summaries);

    when(stockCardSummariesV2DtoBuilder
        .build(summaries.getPageOfApprovedProducts(),
            summaries.getStockCardsForFulfillOrderables(),
            summaries.getOrderableFulfillMap(),
            true))
        .thenReturn(singletonList(stockCardSummary));

    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString())
            .param(NON_EMPTY_ONLY, Boolean.toString(params.isNonEmptyOnly())));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(CONTENT_REFERENCE, hasSize(1)))
        .andExpect(jsonPath(NUMBER_OF_ELEMENTS_REFERENCE, is(1)))
        .andExpect(jsonPath(NUMBER_REFERENCE, is(pageable.getPageNumber())))
        .andExpect(jsonPath(SIZE_REFERENCE, is(pageable.getPageSize())))
        .andExpect(jsonPath(ORDERABLE_ID_REFERENCE,
            is(stockCardSummary.getOrderable().getId().toString())));

    params.setNonEmptyOnly(false);

    when(stockCardSummariesV2DtoBuilder
        .build(summaries.getPageOfApprovedProducts(),
            summaries.getStockCardsForFulfillOrderables(),
            summaries.getOrderableFulfillMap(),
            false))
        .thenReturn(asList(stockCardSummary, stockCardSummary2));

    resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString())
            .param(AS_OF_DATE, params.getAsOfDate().toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableIds().get(1).toString())
            .param(NON_EMPTY_ONLY, Boolean.toString(params.isNonEmptyOnly())));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(CONTENT_REFERENCE, hasSize(2)))
        .andExpect(jsonPath(NUMBER_OF_ELEMENTS_REFERENCE, is(2)))
        .andExpect(jsonPath(NUMBER_REFERENCE, is(pageable.getPageNumber())))
        .andExpect(jsonPath(SIZE_REFERENCE, is(pageable.getPageSize())))
        .andExpect(jsonPath(ORDERABLE_ID_REFERENCE,
            is(stockCardSummary.getOrderable().getId().toString())))
        .andExpect(jsonPath("$.content[1].orderable.id",
            is(stockCardSummary2.getOrderable().getId().toString())));
  }
}
