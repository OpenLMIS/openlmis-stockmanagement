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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

public class PhysicalInventoryControllerTest extends BaseWebTest {
  private static final String PHYSICAL_INVENTORY_API = "/api/physicalInventories";
  private static final String PHYSICAL_INVENTORY_DRAFT_API = "/api/physicalInventories/draft";

  @MockBean
  private PhysicalInventoryService physicalInventoryService;

  @MockBean
  private PermissionService permissionService;

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
    doNothing().when(permissionService).canEditPhysicalInventory(programId, facilityId);

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

  @Test
  public void should_return_201_when_saved_physical_inventory_draft() throws Exception {
    //given
    PhysicalInventoryDto physicalInventoryDto = PhysicalInventoryDto
        .builder()
        .lineItems(singletonList(new PhysicalInventoryLineItemDto()))
        .build();

    when(physicalInventoryService.saveDraft(physicalInventoryDto)).thenReturn(physicalInventoryDto);
    doNothing().when(permissionService).canEditPhysicalInventory(any(), any());

    //when
    ResultActions resultActions = mvc.perform(
        post(PHYSICAL_INVENTORY_DRAFT_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(physicalInventoryDto)));

    //then
    resultActions.andExpect(status().isCreated())
        .andExpect(jsonPath("$.lineItems", hasSize(1)));
  }

  private ResultActions callApi(PhysicalInventoryDto piDto) throws Exception {
    return mvc.perform(post(PHYSICAL_INVENTORY_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(piDto)));
  }
}