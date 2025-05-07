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

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class VvmValidatorTest {

  private static final String ERROR_MESSAGE = "message";

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private VvmValidator validator;

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectIfOrderableDisabledVvmAndLineItemHasVvmStatus() {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "false"));

    VvmApplicable lineItem = generateVvmApplicable(orderable);
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE, false);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectIfOrderableNotConfiguredVvmAndLineItemHasVvmStatus() {
    OrderableDto orderable = generateOrderable();

    VvmApplicable lineItem = generateVvmApplicable(orderable);
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE, false);
  }

  @Test
  public void shouldNotRejectIfOrderableEnabledVvmAndLineItemHasVvmStatus() {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "true"));

    VvmApplicable lineItem = generateVvmApplicable(orderable);
    lineItem.setExtraData(Collections.singletonMap("vvmStatus", "status"));

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE, false);
  }

  @Test
  public void shouldNotRejectIfOrderableDisabledVvmAndLineItemHasNoVvmStatus() {
    OrderableDto orderable = generateOrderable();
    orderable.setExtraData(Collections.singletonMap("useVVM", "false"));

    VvmApplicable lineItem = generateVvmApplicable(orderable);

    validator.validate(Collections.singletonList(lineItem), ERROR_MESSAGE, false);
  }

  private OrderableDto generateOrderable() {
    OrderableDto orderable = new OrderableDto();
    orderable.setId(UUID.randomUUID());

    given(orderableReferenceDataService.findByIds(anyCollection()))
        .willReturn(Collections.singletonList(orderable));

    return orderable;
  }

  private VvmApplicable generateVvmApplicable(OrderableDto orderable) {
    PhysicalInventoryLineItemDto lineItem = new PhysicalInventoryLineItemDto();
    lineItem.setOrderableId(orderable.getId());

    return lineItem;
  }
}
