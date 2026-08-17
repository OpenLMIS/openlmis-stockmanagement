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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_VALIDATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_EVENT_NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.StockEventCancelDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.service.StockEventCancelService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

public class StockEventCancelIntegrationTest extends BaseWebTest {

  private static final String CANCEL_API = "/api/stockEvents/{id}/cancel";

  @MockBean
  private StockEventCancelService stockEventCancelService;

  @Test
  public void shouldReturn201WhenLineItemsCancelled() throws Exception {
    UUID cancellationId = UUID.randomUUID();
    when(stockEventCancelService.cancel(any(UUID.class), any(StockEventCancelDto.class)))
        .thenReturn(cancellationId);

    mvc.perform(post(CANCEL_API, UUID.randomUUID())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventCancelDto())))
        .andExpect(status().isCreated())
        .andExpect(content().string("\"" + cancellationId.toString() + "\""));
  }

  @Test
  public void shouldReturn403WhenUserHasNoCancelPermission() throws Exception {
    when(stockEventCancelService.cancel(any(UUID.class), any(StockEventCancelDto.class)))
        .thenThrow(new PermissionMessageException(new Message(ERROR_NO_FOLLOWING_PERMISSION)));

    mvc.perform(post(CANCEL_API, UUID.randomUUID())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventCancelDto())))
        .andExpect(status().isForbidden());
  }

  @Test
  public void shouldReturn404WhenEventDoesNotExist() throws Exception {
    when(stockEventCancelService.cancel(any(UUID.class), any(StockEventCancelDto.class)))
        .thenThrow(new ResourceNotFoundException(new Message(ERROR_STOCK_EVENT_NOT_FOUND)));

    mvc.perform(post(CANCEL_API, UUID.randomUUID())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventCancelDto())))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturn400WithLineErrorsWhenValidationFails() throws Exception {
    StockEventCancellationLineErrorDto lineError = new StockEventCancellationLineErrorDto(
        UUID.randomUUID(), ERROR_EVENT_LINE_ITEM_NOT_CANCELLABLE, null, null);
    when(stockEventCancelService.cancel(any(UUID.class), any(StockEventCancelDto.class)))
        .thenThrow(new StockEventCancellationException(
            ERROR_EVENT_CANCELLATION_VALIDATION, Collections.singletonList(lineError)));

    mvc.perform(post(CANCEL_API, UUID.randomUUID())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventCancelDto())))
        .andExpect(status().isBadRequest());
  }
}
