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

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_DUPLICATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class PhysicalInventoryControllerTest extends BaseWebTest {
  private static final String PHYSICAL_INVENTORY_API = "/api/physicalInventories";

  @MockBean
  private StockEventProcessor stockEventProcessor;

  @MockBean
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void should_return_400_when_line_items_not_exist() throws Exception {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    piDto.setLineItems(null);
    //error if line items are null
    testValidation(piDto, ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING);

    piDto.setLineItems(new ArrayList<>());
    //error if 0 line items
    testValidation(piDto, ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING);
  }

  @Test
  public void should_return_400_if_orderable_not_exist() throws Exception {
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piDto.setLineItems(singletonList(piLineItemDto));

    //error if line item does not have orderable
    testValidation(piDto, ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING);
  }

  @Test
  public void should_return_400_if_any_orderable_is_duplicate() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);

    UUID orderableId = UUID.randomUUID();
    PhysicalInventoryLineItemDto piLineItemDto1 = new PhysicalInventoryLineItemDto();
    piLineItemDto1.setOrderableDto(OrderableDto.builder().id(orderableId).build());

    PhysicalInventoryLineItemDto piLineItemDto2 = new PhysicalInventoryLineItemDto();
    piLineItemDto2.setOrderableDto(OrderableDto.builder().id(orderableId).build());

    piDto.setLineItems(Arrays.asList(piLineItemDto1, piLineItemDto2));

    //when
    testValidation(piDto, ERROR_PHYSICAL_INVENTORY_ORDERABLE_DUPLICATION);
  }

  @Test
  public void should_return_403_if_user_not_have_permission() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setProgramId(randomUUID());
    piDto.setFacilityId(randomUUID());
    doThrow(new PermissionMessageException(new Message("permission error")))
        .when(permissionService)
        .canCreateStockEvent(piDto.getProgramId(), piDto.getFacilityId());

    //when
    ResultActions resultActions = callApi(piDto);

    //then
    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void should_associate_inventory_with_created_events() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    piDto.setIsDraft(false);
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piLineItemDto.setOrderableDto(OrderableDto.builder().id(randomUUID()).build());
    piDto.setLineItems(singletonList(piLineItemDto));

    UUID eventId = UUID.randomUUID();
    UUID inventoryId = UUID.randomUUID();

    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setId(inventoryId);

    when(stockEventProcessor.process(any(StockEventDto.class))).thenReturn(eventId);
    when(physicalInventoriesRepository.save(any(PhysicalInventory.class))).thenReturn(inventory);

    //when
    ResultActions resultActions = callApi(piDto);

    //then
    ArgumentCaptor<PhysicalInventory> inventoryArgumentCaptor = forClass(PhysicalInventory.class);

    verify(physicalInventoriesRepository, times(1)).save(inventoryArgumentCaptor.capture());
    StockEvent event = inventoryArgumentCaptor.getValue().getStockEvents().get(0);

    assertThat(event.getId(), is(eventId));
    resultActions.andExpect(status().isCreated())
        .andExpect(content().string("\"" + inventoryId.toString() + "\""));
  }

  private void testValidation(PhysicalInventoryDto piDto, String messageKey) throws Exception {
    //when
    ResultActions resultActions = callApi(piDto);

    //then
    resultActions
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.messageKey", is(messageKey)));
  }

  private ResultActions callApi(PhysicalInventoryDto piDto) throws Exception {
    return mvc.perform(post(PHYSICAL_INVENTORY_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(piDto)));
  }
}