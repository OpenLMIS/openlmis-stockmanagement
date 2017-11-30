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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardLineItemReasonService;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Arrays;
import java.util.UUID;


public class StockCardLineItemReasonControllerIntegrationTest extends BaseWebTest {

  private static final String STOCK_CARD_LINE_ITEM_REASON_API = "/api/stockCardLineItemReasons";

  @MockBean
  private StockCardLineItemReasonService stockCardLineItemReasonService;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void shouldReturn201WhenReasonSuccessfullyCreated() throws Exception {
    //given
    StockCardLineItemReason createdReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .build();

    when(stockCardLineItemReasonService.saveOrUpdate(any(StockCardLineItemReason.class)))
        .thenReturn(createdReason);

    //when
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post(STOCK_CARD_LINE_ITEM_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(createdReason)));

    //then
    resultActions
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is(createdReason.getName())))
        .andExpect(jsonPath("$.description", is(createdReason.getDescription())))
        .andExpect(jsonPath("$.reasonType",
            is(createdReason.getReasonType().toString())))
        .andExpect(jsonPath("$.reasonCategory",
            is(createdReason.getReasonCategory().toString())))
        .andExpect(jsonPath("$.isFreeTextAllowed",
            is(createdReason.getIsFreeTextAllowed())));
  }

  @Test
  public void shouldReturn200WhenReasonSuccessfullyUpdated() throws Exception {
    //given
    StockCardLineItemReason updatedReason = new StockCardLineItemReasonDataBuilder()
        .withDescription("test reason")
        .build();

    when(stockCardLineItemReasonService.saveOrUpdate(any(StockCardLineItemReason.class)))
        .thenReturn(updatedReason);

    //when
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .put(STOCK_CARD_LINE_ITEM_REASON_API + "/" + updatedReason.getId().toString())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(updatedReason)));

    //then
    resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(updatedReason.getId().toString())))
        .andExpect(jsonPath("$.name", is(updatedReason.getName())))
        .andExpect(jsonPath("$.description", is(updatedReason.getDescription())))
        .andExpect(jsonPath("$.reasonType",
            is(updatedReason.getReasonType().toString())))
        .andExpect(jsonPath("$.reasonCategory",
            is(updatedReason.getReasonCategory().toString())))
        .andExpect(jsonPath("$.isFreeTextAllowed",
            is(updatedReason.getIsFreeTextAllowed())));
  }

  @Test
  public void shouldReturn200WhenUserGetAllReasons() throws Exception {
    //given
    StockCardLineItemReason reason1 = new StockCardLineItemReasonDataBuilder().build();

    StockCardLineItemReason reason2 = new StockCardLineItemReasonDataBuilder()
        .withName("Another test reason")
        .build();

    when(stockCardLineItemReasonService.findReasons()).thenReturn(Arrays.asList(reason1, reason2));

    //when
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get(STOCK_CARD_LINE_ITEM_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void shouldReturn403WhenUserHasNoPermissionToManageReasons()
      throws Exception {
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageReasons();

    //1.create reason
    ResultActions postResults = mvc.perform(MockMvcRequestBuilders
        .post(STOCK_CARD_LINE_ITEM_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockCardLineItemReason())));
    postResults.andExpect(status().isForbidden());

    //2.update reason
    ResultActions putResults = mvc.perform(MockMvcRequestBuilders
        .put(STOCK_CARD_LINE_ITEM_REASON_API + "/" + UUID.randomUUID().toString())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockCardLineItemReason())));
    putResults.andExpect(status().isForbidden());
  }
}