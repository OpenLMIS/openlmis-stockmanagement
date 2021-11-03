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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockCardDtoDataBuilder.createStockCardDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.ImmutableSet;
import java.util.UUID;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardService;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.testutils.StockCardDtoDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.ResultActions;


//the name of this controller test is intentional wrong: cardz insteads of cards
//because there is a problem with "spring security test" that seems to be relates
//with test execution order, naming it cardz will put it behind and solve the problem
//not sure what the root cause is yet
public class StockCardControllerIntegrationTest extends BaseWebTest {

  private static final String API_STOCK_CARDS = "/api/stockCards/";
  private static final String API_STOCK_CARD_SUMMARIES = "/api/stockCardSummaries";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String ID = "id";
  private static final String INACTIVE = "/deactivate";

  @MockBean
  private StockCardService stockCardService;

  @MockBean
  private StockCardSummariesService stockCardSummariesService;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void should404WhenStockCardNotFoundById() throws Exception {
    //given
    when(stockCardService.findStockCardById(any(UUID.class))).thenReturn(null);

    //when
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARDS + UUID.randomUUID().toString())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  public void shouldGetStockCardById() throws Exception {
    //given
    UUID stockCardId = UUID.randomUUID();
    StockCardDto stockCardDto = createStockCardDto();

    when(stockCardService.findStockCardById(stockCardId)).thenReturn(stockCardDto);

    //when
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARDS + stockCardId.toString())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content()
            .json("{"
                + "'stockOnHand':1,"
                + "'facility':{'name':'HC01'},"
                + "'program':{'name':'HIV'},"
                + "'orderable':{'productCode':'ABC01'},"
                + "'lineItems':["
                + "{'occurredDate':'2017-02-13',"
                + "'source':{'name':'HF1'},"
                + "'destination':null,"
                + "'reason':{'name':'Transfer In','reasonCategory':'ADJUSTMENT',"
                + "'reasonType':'CREDIT'},"
                + "'quantity':1, 'stockOnHand':1}]}"));
  }

  @Test
  public void shouldReturn403WhenUserDoesNotHavePermissionToViewCardSummaries()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    doThrow(new
        PermissionMessageException(new Message("no permission"))).when(permissionService)
        .canViewStockCard(programId, facilityId);

    //when
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param("program", programId.toString())
            .param("facility", facilityId.toString()));

    //then
    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void shouldGetPagedStockCardSummariesWhenPermissionIsGranted() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    PageRequest pageable = PageRequest.of(0, 20);
    when(stockCardSummariesService
        .findStockCards(programId, facilityId, pageable))
        .thenReturn(new PageImpl<>(singletonList(StockCardDtoDataBuilder.createStockCardDto())));

    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARD_SUMMARIES)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param("page", "0")
            .param("size", "20")
            .param("program", programId.toString())
            .param("facility", facilityId.toString()));

    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void shouldGetPagedStockCards() throws Exception {
    UUID stockCardId1 = UUID.randomUUID();
    UUID stockCardId2 = UUID.randomUUID();

    Pageable pageable = PageRequest.of(0, 10);

    doReturn(new PageImpl<>(singletonList(StockCardDtoDataBuilder.createStockCardDto())))
        .when(stockCardService).search(ImmutableSet.of(stockCardId1, stockCardId2), pageable);
    ResultActions resultActions = mvc.perform(
        get(API_STOCK_CARDS)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(ID, stockCardId1.toString())
            .param(ID, stockCardId2.toString())
            .param(PAGE, String.valueOf(pageable.getPageNumber()))
            .param(SIZE, String.valueOf(pageable.getPageSize())));

    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void shouldMakeStockCardInactive() throws Exception {
    // given
    UUID stockCardId = UUID.randomUUID();
    doNothing().when(stockCardService).setInactive(stockCardId);

    // when
    ResultActions resultActions = mvc.perform(
        put(API_STOCK_CARDS + stockCardId.toString() + INACTIVE)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    // then
    resultActions.andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  public void shouldReturn404WhenStockCardNotFoundWhileMakingInactive() throws Exception {
    // given
    UUID stockCardId = UUID.randomUUID();
    doThrow(new ResourceNotFoundException("Not found stock card with id: " + stockCardId))
        .when(stockCardService).setInactive(stockCardId);

    // when
    ResultActions resultActions = mvc.perform(
        put(API_STOCK_CARDS + stockCardId.toString() + INACTIVE)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    // then
    resultActions.andExpect(status().isNotFound());
  }
}