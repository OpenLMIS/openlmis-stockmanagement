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

import org.junit.Test;
import org.mockito.Matchers;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.service.StockCardService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockCardDtoBuilder.createStockCardDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//the name of this controller test is intentional wrong: cardz insteads of cards
//because there is a problem with "spring security test" that seems to be relates
//with test execution order, naming it cardz will put it behind and solve the problem
//not sure what the root cause is yet
public class StockCardzControllerTest extends BaseWebTest {

  private static final String API_STOCK_CARDS = "/api/stockCards/";

  @MockBean
  private StockCardService stockCardService;

  @Test
  public void should_404_when_stock_card_not_found_by_id() throws Exception {
    //given
    when(stockCardService.findStockCardById(Matchers.any(UUID.class))).thenReturn(null);

    //when
    ResultActions resultActions = mvc.perform(
            get(API_STOCK_CARDS + UUID.randomUUID().toString())
                    .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  public void should_get_stock_card_by_id() throws Exception {
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
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content()
                    .json("{"
                            + "'stockOnHand':1,"
                            + "'facility':{'name':'HC01'},"
                            + "'program':{'name':'HIV'},"
                            + "'orderable':{'productCode':'ABC01'},"
                            + "'lineItems':["
                            + "{'occurredDate':'2017-02-13T04:02:18.781+0000',"
                            + "'source':{'name':'HF1'},"
                            + "'destination':null,"
                            + "'reason':{'name':'Transfer In','reasonCategory':'ADJUSTMENT',"
                            + "'reasonType':'CREDIT'},"
                            + "'quantity':1, 'stockOnHand':1}]}"));
  }

}