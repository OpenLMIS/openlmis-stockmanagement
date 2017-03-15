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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

public class PhysicalInventoryControllerTest extends BaseWebTest {
  private static final String PHYSICAL_INVENTORY_API = "/api/physicalInventories";
  private static final String PHYSICAL_INVENTORY_DRAFT_API = "/api/physicalInventories/draft";

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private PhysicalInventoryService physicalInventoryService;

  @Test
  public void should_return_403_if_user_not_has_permission() throws Exception {
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
  public void should_return_400_when_validation_failed() throws Exception {
    //given
    doThrow(new ValidationMessageException(new Message("key")))
        .when(physicalInventoryService).submitPhysicalInventory(any(PhysicalInventoryDto.class));

    //when
    ResultActions resultActions = callApi(new PhysicalInventoryDto());

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void should_return_201_when_physical_inventory_successfully_created() throws Exception {
    //given
    PhysicalInventoryDto piDto = new PhysicalInventoryDto();
    PhysicalInventoryLineItemDto piLineItemDto = new PhysicalInventoryLineItemDto();
    piLineItemDto.setOrderable(OrderableDto.builder().id(randomUUID()).build());
    piDto.setLineItems(singletonList(piLineItemDto));

    UUID inventoryId = UUID.randomUUID();
    when(physicalInventoryService.submitPhysicalInventory(any(PhysicalInventoryDto.class)))
        .thenReturn(inventoryId);

    //when
    ResultActions resultActions = callApi(piDto);

    //then
    resultActions.andExpect(status().isCreated())
        .andExpect(content().string("\"" + inventoryId.toString() + "\""));
  }

  @Test
  public void should_return_200_when_found_physical_inventory_draft() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto physicalInventoryDto = PhysicalInventoryDto
        .builder()
        .programId(programId)
        .facilityId(facilityId)
        .lineItems(singletonList(new PhysicalInventoryLineItemDto()))
        .build();
    when(physicalInventoryService.findDraft(programId, facilityId))
        .thenReturn(physicalInventoryDto);

    //when
    ResultActions resultActions = mvc.perform(
        get(PHYSICAL_INVENTORY_DRAFT_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param("program", programId.toString())
            .param("facility", facilityId.toString())
            .contentType(MediaType.APPLICATION_JSON));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lineItems", hasSize(1)));
  }

  private ResultActions callApi(PhysicalInventoryDto piDto) throws Exception {
    return mvc.perform(post(PHYSICAL_INVENTORY_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(piDto)));
  }
}