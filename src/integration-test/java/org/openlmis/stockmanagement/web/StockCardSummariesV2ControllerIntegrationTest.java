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

package org.openlmis.stockmanagement.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.StockCardSummaryV2Dto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams;
import org.openlmis.stockmanagement.testutils.StockCardSummariesV2SearchParamsDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardSummaryV2DtoDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Collections;

public class StockCardSummariesV2ControllerIntegrationTest extends BaseWebTest {

  private static final String API_STOCK_CARD_SUMMARIES = "/api/v2/stockCardSummaries";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String AS_OF_DATE = "asOfDate";
  private static final String ORDERABLE_ID = "orderableId";

  @MockBean
  private StockCardSummariesService stockCardSummariesService;

  @MockBean
  private PermissionService permissionService;

  private StockCardSummaryV2Dto stockCardSummary;
  private StockCardSummariesV2SearchParams params;
  private Pageable pageable;

  @Before
  public void setUp() {
    stockCardSummary = new StockCardSummaryV2DtoDataBuilder().build();
    params = new StockCardSummariesV2SearchParamsDataBuilder().build();
    pageable = new PageRequest(0, 10);

    when(stockCardSummariesService.findStockCards(any(StockCardSummariesV2SearchParams.class),
        any(Pageable.class)))
        .thenReturn(
            new PageImpl<>(Collections.singletonList(stockCardSummary), pageable, 1));
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
            .param(ORDERABLE_ID, params.getOrderableId().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableId().get(1).toString()));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)));
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
            .param(ORDERABLE_ID, params.getOrderableId().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableId().get(1).toString()));

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
            .param(ORDERABLE_ID, params.getOrderableId().get(0).toString())
            .param(ORDERABLE_ID, params.getOrderableId().get(1).toString()));

    resultActions
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldReturnForbiddenIfNoPermission() throws Exception {
    doThrow(new
        PermissionMessageException(new Message("no permission"))).when(permissionService)
        .canViewStockCard(params.getProgramId(), params.getFacilityId());

    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize()))
            .param(PROGRAM_ID, params.getProgramId().toString())
            .param(FACILITY_ID, params.getFacilityId().toString()));

    resultActions.andExpect(status().isForbidden());
  }
}
