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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_QUANTITIES_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_SUPPORTED;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_ADJUST;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createNoSourceDestinationStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.HomeFacilityPermissionService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

/**
 * TODO: needs to merge with StockEventsControllerIntegrationTest.
 */
public class StockEventsControllerrIntegrationTest extends BaseWebTest {

  private static final String CREATE_STOCK_EVENT_API = "/api/stockEvents";

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @MockBean
  private StockEventProcessor stockEventProcessor;

  @Test
  public void shouldReturn201WhenEventSuccessfullyCreated() throws Exception {
    //given
    UUID uuid = UUID.randomUUID();
    when(stockEventProcessor.process(any(StockEventDto.class)))
        .thenReturn(uuid);

    //when
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(null);

    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(stockEventDto)));

    //then
    resultActions.andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated())
        .andExpect(content().string("\"" + uuid.toString() + "\""));
  }

  @Test
  public void shouldReturn403WhenUserHasNoPermissionToAdjustStock() throws Exception {
    //given
    when(homeFacilityPermissionService.checkFacilityAndHomeFacilityLinkage(any(UUID.class)))
        .thenReturn(false);
    Mockito.doThrow(new PermissionMessageException(
        new Message(ERROR_NO_FOLLOWING_PERMISSION, STOCK_ADJUST)))
        .when(permissionService).canAdjustStock(any(UUID.class), any(UUID.class));

    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    shouldReject(eventDto);
  }

  @Test
  public void shouldReturn403WhenUserHomeFacilityDoesNotSupportProvidedProgram()
      throws Exception {
    //given
    StockEventDto eventDto = createNoSourceDestinationStockEventDto();

    Mockito.doThrow(new PermissionMessageException(
        new Message(ERROR_PROGRAM_NOT_SUPPORTED)))
        .when(homeFacilityPermissionService).checkProgramSupported(eventDto.getProgramId());

    shouldReject(eventDto);
  }

  @Test
  public void shouldReturn403WhenUserHasNoPermissionToPerformPhysicalInventory()
      throws Exception {
    //given
    Mockito.doThrow(new PermissionMessageException(
        new Message(ERROR_NO_FOLLOWING_PERMISSION, STOCK_ADJUST)))
        .when(permissionService).canEditPhysicalInventory(any(UUID.class), any(UUID.class));

    StockEventDto dto = createNoSourceDestinationStockEventDto();
    dto.getLineItems().get(0).setReasonId(null);
    shouldReject(dto);
  }

  @Test
  public void shouldReturn400WhenValidationFails() throws Exception {
    //given
    Mockito.doThrow(new ValidationMessageException(new Message(ERROR_EVENT_QUANTITIES_INVALID)))
        .when(stockEventProcessor).process(any());

    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventDto())));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  private void shouldReject(StockEventDto eventDto) throws Exception {
    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(eventDto)));

    //then
    resultActions.andExpect(status().isForbidden());
  }
}