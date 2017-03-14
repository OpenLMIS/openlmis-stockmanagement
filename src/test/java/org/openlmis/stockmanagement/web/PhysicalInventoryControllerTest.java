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
import static org.hamcrest.core.Is.is;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;

public class PhysicalInventoryControllerTest extends BaseWebTest {
  private static final String PHYSICAL_INVENTORY_API = "/api/physicalInventories";

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

  private void testValidation(PhysicalInventoryDto piDto, String messageKey) throws Exception {
    //when
    ResultActions resultActions = mvc.perform(post(PHYSICAL_INVENTORY_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(piDto)));

    //then
    resultActions
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.messageKey", is(messageKey)));
  }
}