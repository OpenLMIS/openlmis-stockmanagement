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
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

import java.util.Collections;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class VvmValidatorTest {

  private static final String ERROR_MESSAGE = "message";

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private VvmValidator validator;

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectIfOrderableDisabledVvmAndLineItemHasVvmStatus()
      throws InstantiationException, IllegalAccessException {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "false"));

    VvmApplicable lineItem = generateVvmApplicable(orderable);
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectIfOrderableNotConfiguredVvmAndLineItemHasVvmStatus()
      throws IllegalAccessException, InstantiationException {
    OrderableDto orderable = generateOrderable();

    VvmApplicable lineItem = generateVvmApplicable(orderable);
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE);
  }

  @Test
  public void shouldNotRejectIfOrderableEnabledVvmAndLineItemHasVvmStatus()
      throws IllegalAccessException, InstantiationException {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "true"));

    VvmApplicable lineItem = generateVvmApplicable(orderable);
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE);
  }

  @Test
  public void shouldNotRejectIfOrderableDisabledVvmAndLineItemHasNoVvmStatus()
      throws IllegalAccessException, InstantiationException {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "false"));

    VvmApplicable lineItem = generateVvmApplicable(orderable);

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE);
  }

  private OrderableDto generateOrderable() {
    OrderableDto orderable = new OrderableDto();
    orderable.setId(UUID.randomUUID());

    given(orderableReferenceDataService.findOne(eq(orderable.getId())))
        .willReturn(orderable);

    return orderable;
  }

  private VvmApplicable generateVvmApplicable(OrderableDto orderable) {
    PhysicalInventoryLineItemDto lineItem = new PhysicalInventoryLineItemDto();
    lineItem.setOrderable(orderable);

    return lineItem;
  }
}
