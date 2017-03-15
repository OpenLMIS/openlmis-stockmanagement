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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

public class ReportsControllerTest extends BaseWebTest {

  private static final String STOCK_CARD_REPORT = "/api/stockCards/%s/print";

  @MockBean
  private JasperReportService reportService;

  @Test
  public void return_200_when_stock_card_report_generated() throws Exception {
    //given
    UUID stockCardId = UUID.randomUUID();
    when(reportService.getStockCardReportView(stockCardId))
        .thenReturn(new ModelAndView("stockCardReportPDF"));

    //when
    ResultActions resultActions = mvc.perform(
        get(String.format(STOCK_CARD_REPORT, stockCardId.toString()))
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }
}