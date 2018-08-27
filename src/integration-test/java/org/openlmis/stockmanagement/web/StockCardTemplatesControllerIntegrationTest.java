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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_TYPE_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_CARD_TEMPLATES_MANAGE;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateDataBuilder.createTemplateDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardTemplateService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class StockCardTemplatesControllerIntegrationTest extends BaseWebTest {

  private static final String STOCK_CARD_TEMPLATE_API = "/api/stockCardTemplates";

  @MockBean
  private StockCardTemplateService stockCardTemplateService;
  @MockBean
  private PermissionService permissionService;

  private UUID programId = UUID.randomUUID();
  private UUID facilityTypeId = UUID.randomUUID();

  @Test
  public void shouldGetDefaultStockCardTemplatesWithoutParams() throws Exception {
    //given
    when(stockCardTemplateService.getDefaultStockCardTemplate())
        .thenReturn(createTemplateDto());

    //when
    MockHttpServletRequestBuilder builder = get(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE);

    ResultActions resultActions = mvc.perform(builder);

    //then
    resultActions
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content()
            .json("{'stockCardFields':[{'name':'packSize'}]}"));
  }

  @Test
  public void shouldSearchForStockCardTemplates() throws Exception {

    //given
    when(stockCardTemplateService.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(createTemplateDto());

    //when
    MockHttpServletRequestBuilder builder = get(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", programId.toString())
        .param("facilityType", facilityTypeId.toString());

    ResultActions resultActions = mvc.perform(builder);

    //then
    resultActions
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().json("{'stockCardFields':[{'name':'packSize', displayed:true}]}"));
  }

  @Test
  public void shouldReturn404WhenTemplateNotFound() throws Exception {
    //given
    when(stockCardTemplateService.findByProgramIdAndFacilityTypeId(any(), any()))
        .thenReturn(null);

    //when
    ResultActions resultActions = mvc.perform(get(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", UUID.randomUUID().toString())
        .param("facilityType", UUID.randomUUID().toString()));

    //then
    resultActions
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturn201WhenCreateTemplate() throws Exception {

    //given
    Mockito.doNothing().when(permissionService).canCreateStockCardTemplate();
    when(stockCardTemplateService.saveOrUpdate(any(StockCardTemplateDto.class)))
        .thenReturn(createTemplateDto());

    //when
    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockCardTemplate())));

    //then
    resultActions.andExpect(status().isCreated());
  }

  @Test
  public void shouldReturn403WhenCreateTemplatePermissionNotFound() throws Exception {
    //given
    Mockito.doThrow(new PermissionMessageException(
        new Message(ERROR_NO_FOLLOWING_PERMISSION, STOCK_CARD_TEMPLATES_MANAGE)))
        .when(permissionService).canCreateStockCardTemplate();

    //when
    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockCardTemplate())));

    //then
    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void shouldReturn401WhenUserUnauthorized() throws Exception {
    //given
    Mockito.doThrow(new AuthenticationException("MANAGE_STOCK_CARD_TEMPLATES"))
        .when(permissionService).canCreateStockCardTemplate();

    //when
    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockCardTemplate())));

    //then
    resultActions.andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldReturn400WhenProgramNotFound() throws Exception {
    throwValidationExceptionWith(ERROR_PROGRAM_NOT_FOUND);
  }

  @Test
  public void shouldReturn400WhenFacilityTypeNotFound() throws Exception {
    throwValidationExceptionWith(ERROR_FACILITY_TYPE_NOT_FOUND);
  }

  private void throwValidationExceptionWith(String exceptionKey) throws Exception {
    //given
    Mockito.doThrow(new ValidationMessageException(
        new Message(exceptionKey, "some id")))
        .when(stockCardTemplateService).saveOrUpdate(any());

    //when
    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockCardTemplate())));

    //then
    resultActions.andExpect(status().isBadRequest());
  }
}