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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidReasonAssignmentDto
    implements ValidReasonAssignment.Exporter, ValidReasonAssignment.Importer {
  private UUID id;
  private UUID programId;
  private UUID facilityTypeId;
  private Boolean hidden;
  private StockCardLineItemReason reason;

  /**
   * Creates new instance based on data from {@link ValidReasonAssignment}
   *
   * @param validReason instance of {@link ValidReasonAssignment}
   * @return new instance of ValidReasonAssignmentDto.
   */
  public static ValidReasonAssignmentDto newInstance(ValidReasonAssignment validReason) {
    ValidReasonAssignmentDto validReasonDto = new ValidReasonAssignmentDto();
    validReason.export(validReasonDto);
    return validReasonDto;
  }

}
