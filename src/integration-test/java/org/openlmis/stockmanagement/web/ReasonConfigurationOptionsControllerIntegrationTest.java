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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;

public class ReasonConfigurationOptionsControllerIntegrationTest extends BaseWebTest {

  private String reasonTypesApi = "/api/reasonTypes";
  private String reasonCategoriesApi = "/api/reasonCategories";

  @MockBean
  private PermissionService permissionService;

  @Test
  public void shouldGetReasonTypes() throws Exception {
    //when
    ResultActions resultActions = mvc.perform(get(reasonTypesApi)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$.[0]", is("CREDIT")))
        .andExpect(jsonPath("$.[1]", is("DEBIT")));
  }

  @Test
  public void shouldGetReasonCategories() throws Exception {
    //when
    ResultActions resultActions = mvc.perform(get(reasonCategoriesApi)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$.[0]", is("TRANSFER")))
        .andExpect(jsonPath("$.[1]", is("ADJUSTMENT")));
  }

  @Test
  public void shouldReturn403ForReasonTypeAccessIfUserHasNoPermission()
      throws Exception {
    shouldThrow403(reasonTypesApi);
  }

  @Test
  public void shouldReturn403ForReasonCategoryAccessIfUserHasNoPermission()
      throws Exception {
    shouldThrow403(reasonCategoriesApi);
  }

  private void shouldThrow403(String api) throws Exception {
    //given
    doThrow(new PermissionMessageException(new Message("some error")))
        .when(permissionService).canManageReasons();

    //when
    ResultActions resultActions = mvc.perform(get(api)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andExpect(status().isForbidden());
  }
}