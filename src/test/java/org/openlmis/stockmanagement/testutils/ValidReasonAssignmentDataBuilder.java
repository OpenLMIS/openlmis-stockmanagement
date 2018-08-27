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

package org.openlmis.stockmanagement.testutils;

import java.util.UUID;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;

@NoArgsConstructor
public class ValidReasonAssignmentDataBuilder {
  private UUID id = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID facilityTypeId = UUID.randomUUID();
  private StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().build();
  private Boolean hidden = true;

  public ValidReasonAssignmentDataBuilder withoutId() {
    id = null;
    return this;
  }

  public ValidReasonAssignmentDataBuilder withProgram(UUID newProgram) {
    programId = newProgram;
    return this;
  }

  public ValidReasonAssignmentDataBuilder withFacilityType(UUID newFacilityType) {
    facilityTypeId = newFacilityType;
    return this;
  }

  public ValidReasonAssignmentDataBuilder withReason(StockCardLineItemReason newReason) {
    reason = newReason;
    return this;
  }

  /**
   * Creates new instance of {@link ValidReasonAssignment} with properties.
   * @return created valid reason assignment.
   */
  public ValidReasonAssignment build() {
    ValidReasonAssignment assignment = new ValidReasonAssignment(
        programId, facilityTypeId, hidden, reason
    );
    assignment.setId(id);

    return assignment;
  }


}
