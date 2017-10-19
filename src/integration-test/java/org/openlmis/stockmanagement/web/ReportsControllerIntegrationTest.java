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

import static java.lang.String.format;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.ModelAndView;
import java.util.UUID;

public class ReportsControllerIntegrationTest extends BaseWebTest {

  private static final String CARD_REPORT = "/api/stockCards/%s/print";
  private static final String CARD_SUMMARY_REPORT = "/api/stockCardSummaries/print";

  @MockBean
  private JasperReportService reportService;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void return_200_when_stock_card_report_generated() throws Exception {
    //given
    UUID stockCardId = UUID.randomUUID();
    when(reportService.getStockCardReportView(stockCardId))
        .thenReturn(new ModelAndView("stockCardReportPDF"));

    //when
    ResultActions resultActions = mvc.perform(get(format(CARD_REPORT, stockCardId.toString()))
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andExpect(status().isOk());
  }

  @Test
  public void return_200_when_stock_card_summary_report_generated() throws Exception {
    //given
    UUID program = UUID.randomUUID();
    UUID facility = UUID.randomUUID();
    when(reportService.getStockCardSummariesReportView(program, facility))
        .thenReturn(new ModelAndView("stockCardSummaryReportPDF"));

    //when
    ResultActions resultActions = mvc.perform(get(CARD_SUMMARY_REPORT)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", program.toString())
        .param("facility", facility.toString()));

    //then
    resultActions.andExpect(status().isOk());
    verify(permissionService, times(1)).canViewStockCard(program, facility);
  }

  @Test
  public void return_403_when_user_has_no_permission_to_view_stock_card() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("key"))).when(permissionService)
        .canViewStockCard(programId, facilityId);

    //when
    ResultActions resultActions = mvc.perform(get(CARD_SUMMARY_REPORT)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", programId.toString())
        .param("facility", facilityId.toString()));

    //then
    resultActions.andExpect(status().isForbidden());
  }
}