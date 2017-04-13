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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_EVENT_REASON_NOT_MATCH;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_ADJUST;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createNoSourceDestinationStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.UUID;

public class StockEventsControllerTest extends BaseWebTest {

  private static final String CREATE_STOCK_EVENT_API = "/api/stockEvents";

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private StockEventProcessor stockEventProcessor;

  @Test
  public void should_return_201_when_event_successfully_created() throws Exception {
    //given
    UUID uuid = UUID.randomUUID();
    when(stockEventProcessor.process(any(StockEventDto.class)))
        .thenReturn(uuid);

    //when
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setSourceId(null);
    stockEventDto.setDestinationId(null);

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
  public void should_return_403_when_user_has_no_permission_to_adjust_stock() throws Exception {
    //given
    Mockito.doThrow(new PermissionMessageException(
        new Message(ERROR_NO_FOLLOWING_PERMISSION, STOCK_ADJUST)))
        .when(permissionService).canAdjustStock(any(UUID.class), any(UUID.class));

    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    shouldReject(eventDto);
  }

  @Test
  public void should_return_403_when_user_has_no_permission_to_perform_physical_inventory()
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
  public void should_return_403_when_user_try_to_issue_or_receive() throws Exception {
    StockEventDto receiveEventDto = new StockEventDto();
    receiveEventDto.setSourceId(UUID.randomUUID());
    shouldReject(receiveEventDto);

    StockEventDto issueEventDto = new StockEventDto();
    issueEventDto.setDestinationId(UUID.randomUUID());
    shouldReject(issueEventDto);
  }

  @Test
  public void should_return_400_when_validation_fails() throws Exception {
    //given
    Mockito.doThrow(new ValidationMessageException(new Message(ERROR_STOCK_EVENT_REASON_NOT_MATCH)))
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