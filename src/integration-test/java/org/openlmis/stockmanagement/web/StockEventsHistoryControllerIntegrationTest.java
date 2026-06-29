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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.dto.StockEventHistoryDto;
import org.openlmis.stockmanagement.dto.StockEventLineDetailDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventsService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("PMD.TooManyMethods")
public class StockEventsHistoryControllerIntegrationTest extends BaseWebTest {

  private static final String STOCK_EVENTS_URL = "/api/stockEvents";
  private static final String FACILITY_ID = "facilityId";
  private static final String PROGRAM_ID = "programId";

  @MockBean
  private StockEventsService stockEventsService;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void shouldReturn403WhenUserCannotViewStockEvents() throws Exception {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("no permission")))
        .when(permissionService).canViewStockCard(programId, facilityId);

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(FACILITY_ID, facilityId.toString())
            .param(PROGRAM_ID, programId.toString()));

    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void shouldGetPagedStockEventsWhenPermissionGranted() throws Exception {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    StockEventHistoryDto dto = StockEventHistoryDto.builder()
        .id(UUID.randomUUID())
        .documentNumber("2026-06-FAC001-0001")
        .type(EventOrigin.ISSUE)
        .entriesCount(3)
        .build();

    when(stockEventsService.search(any(StockEventSearchParams.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(singletonList(dto)));

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(FACILITY_ID, facilityId.toString())
            .param(PROGRAM_ID, programId.toString())
            .param("type", "issue")
            .param("page", "0")
            .param("size", "20"));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].documentNumber", is("2026-06-FAC001-0001")))
        .andExpect(jsonPath("$.content[0].entriesCount", is(3)));
  }

  @Test
  public void shouldMapIssueTypeFilterToIssueOriginOnly() throws Exception {
    when(stockEventsService.search(any(StockEventSearchParams.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(emptyList()));

    mvc.perform(get(STOCK_EVENTS_URL)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(FACILITY_ID, UUID.randomUUID().toString())
        .param(PROGRAM_ID, UUID.randomUUID().toString())
        .param("type", "issue")).andExpect(status().isOk());

    ArgumentCaptor<StockEventSearchParams> captor =
        ArgumentCaptor.forClass(StockEventSearchParams.class);
    verify(stockEventsService).search(captor.capture(), any(Pageable.class));

    assertThat(captor.getValue().getEventOrigins(), contains(EventOrigin.ISSUE));
  }

  @Test
  public void shouldDefaultToAllOriginsWhenTypeFilterIsBlank() throws Exception {
    when(stockEventsService.search(any(StockEventSearchParams.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(emptyList()));

    mvc.perform(get(STOCK_EVENTS_URL)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(FACILITY_ID, UUID.randomUUID().toString())
        .param(PROGRAM_ID, UUID.randomUUID().toString())).andExpect(status().isOk());

    ArgumentCaptor<StockEventSearchParams> captor =
        ArgumentCaptor.forClass(StockEventSearchParams.class);
    verify(stockEventsService).search(captor.capture(), any(Pageable.class));

    assertThat(captor.getValue().getEventOrigins(),
        containsInAnyOrder(EventOrigin.ISSUE, EventOrigin.RECEIVE));
  }

  @Test
  public void shouldReturn403WhenUserCannotViewStockEventDetail() throws Exception {
    UUID eventId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("no permission")))
        .when(stockEventsService).findStockEventLineItems(eq(eventId), any(Pageable.class));

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL + "/" + eventId + "/lineItems")
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void shouldGetStockEventLineItemsDetail() throws Exception {
    UUID eventId = UUID.randomUUID();
    StockEventLineDetailDto line = StockEventLineDetailDto.builder()
        .quantity(7)
        .stockOnHand(20)
        .build();

    when(stockEventsService.findStockEventLineItems(eq(eventId), any(Pageable.class)))
        .thenReturn(new PageImpl<>(singletonList(line)));

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL + "/" + eventId + "/lineItems")
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].quantity", is(7)));
  }

  @Test
  public void shouldReturnNotFoundWhenStockEventDoesNotExist() throws Exception {
    UUID eventId = UUID.randomUUID();
    doThrow(new ResourceNotFoundException(new Message("stock event not found")))
        .when(stockEventsService).findStockEventLineItems(eq(eventId), any(Pageable.class));

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL + "/" + eventId + "/lineItems")
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturn403WhenUserCannotViewStockEventHeader() throws Exception {
    UUID eventId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("no permission")))
        .when(stockEventsService).findStockEvent(eq(eventId));

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL + "/" + eventId)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void shouldGetStockEventHeader() throws Exception {
    UUID eventId = UUID.randomUUID();
    StockEventHistoryDto dto = StockEventHistoryDto.builder()
        .id(eventId)
        .documentNumber("DOC-1")
        .type(EventOrigin.RECEIVE)
        .build();

    when(stockEventsService.findStockEvent(eq(eventId))).thenReturn(dto);

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL + "/" + eventId)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(eventId.toString())))
        .andExpect(jsonPath("$.type", is(EventOrigin.RECEIVE.toString())))
        .andExpect(jsonPath("$.documentNumber", is("DOC-1")));
  }

  @Test
  public void shouldReturnNotFoundWhenStockEventHeaderDoesNotExist() throws Exception {
    UUID eventId = UUID.randomUUID();
    doThrow(new ResourceNotFoundException(new Message("stock event not found")))
        .when(stockEventsService).findStockEvent(eq(eventId));

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL + "/" + eventId)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturnBadRequestForInvalidType() throws Exception {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    ResultActions resultActions = mvc.perform(
        get(STOCK_EVENTS_URL)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(FACILITY_ID, facilityId.toString())
            .param(PROGRAM_ID, programId.toString())
            .param("type", "foo"));

    resultActions.andExpect(status().isBadRequest());
  }
}
