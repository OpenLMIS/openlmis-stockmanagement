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

package org.openlmis.stockmanagement.validators;


import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

import java.util.Collections;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockEventVvmValidatorTest {

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private StockEventVvmValidator validator;

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectIfOrderableDisabledVvmAndLineItemHasVvmStatus()
      throws InstantiationException, IllegalAccessException {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "false"));

    StockEventLineItem lineItem = generateStockEventLineItem(orderable.getId());
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    StockEventDto stockEvent = new StockEventDto();
    stockEvent.setLineItems(Collections.singletonList(lineItem));

    validator.validate(stockEvent);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectIfOrderableNotConfiguredVvmAndLineItemHasVvmStatus()
      throws InstantiationException, IllegalAccessException {
    OrderableDto orderable = generateOrderable();

    StockEventLineItem lineItem = generateStockEventLineItem(orderable.getId());
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    StockEventDto stockEvent = new StockEventDto();
    stockEvent.setLineItems(Collections.singletonList(lineItem));

    validator.validate(stockEvent);
  }

  @Test
  public void shouldNotRejectIfOrderableEnabledVvmAndLineItemHasVvmStatus()
      throws InstantiationException, IllegalAccessException {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "true"));

    StockEventLineItem lineItem = generateStockEventLineItem(orderable.getId());
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    StockEventDto stockEvent = new StockEventDto();
    stockEvent.setLineItems(Collections.singletonList(lineItem));

    validator.validate(stockEvent);
  }

  @Test
  public void shouldNotRejectIfOrderableDisabledVvmAndLineItemHasNoVvmStatus()
      throws InstantiationException, IllegalAccessException {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "false"));

    StockEventLineItem lineItem = generateStockEventLineItem(orderable.getId());

    StockEventDto stockEvent = new StockEventDto();
    stockEvent.setLineItems(Collections.singletonList(lineItem));

    validator.validate(stockEvent);
  }

  private OrderableDto generateOrderable() {
    OrderableDto orderable = new OrderableDto();
    orderable.setId(UUID.randomUUID());

    given(orderableReferenceDataService.findOne(eq(orderable.getId())))
        .willReturn(orderable);

    return orderable;
  }

  private StockEventLineItem generateStockEventLineItem(UUID orderableId) {
    StockEventLineItem lineItem = new StockEventLineItem();
    lineItem.setOrderableId(orderableId);

    return lineItem;
  }
}
