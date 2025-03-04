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

package org.openlmis.stockmanagement.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.testutils.ValidReasonAssignmentDataBuilder;

public class ValidReasonAssignmentDtoTest {
  @Test
  public void shouldCreateFromEntity() {
    final ValidReasonAssignment validReason = new ValidReasonAssignmentDataBuilder().build();
    final ValidReasonAssignmentDto dto = ValidReasonAssignmentDto.newInstance(validReason);

    assertEquals(validReason.getId(), dto.getId());
    assertEquals(validReason.getProgramId(), dto.getProgramId());
    assertEquals(validReason.getFacilityTypeId(), dto.getFacilityTypeId());
    assertEquals(validReason.getHidden(), dto.getHidden());
    assertEquals(StockCardLineItemReasonDto.newInstance(validReason.getReason()), dto.getReason());
  }
}
