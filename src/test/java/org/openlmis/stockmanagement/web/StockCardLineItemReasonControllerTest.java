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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDtoBuilder.createDto;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.service.StockCardLineItemReasonService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


public class StockCardLineItemReasonControllerTest extends BaseWebTest {

  private static final String STOCK_CARD_LINE_ITEM_REASON_API = "/api/stockCardLineItemReasons";

  @MockBean
  private StockCardLineItemReasonService stockCardLineItemReasonService;

  @Test
  public void should_return_201_when_reason_successfully_created() throws Exception {
    //given
    when(stockCardLineItemReasonService.saveOrUpdate(any(StockCardLineItemReasonDto.class)))
        .thenReturn(createDto());

    //when
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post(STOCK_CARD_LINE_ITEM_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(createDto())));

    //then
    resultActions
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Donation")))
        .andExpect(jsonPath("$.description", is("Donation from the donor")))
        .andExpect(jsonPath("$.reasonType", is("CREDIT")))
        .andExpect(jsonPath("$.reasonCategory", is("AD_HOC")))
        .andExpect(jsonPath("$.isFreeTextAllowed", is(true)));
  }

}