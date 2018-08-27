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

import static org.openlmis.stockmanagement.service.ResourceNames.FACILITY_TYPES;
import static org.openlmis.stockmanagement.service.ResourceNames.PROGRAMS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;

@AllArgsConstructor
@NoArgsConstructor
public class ValidReasonAssignmentDto
    implements ValidReasonAssignment.Exporter, ValidReasonAssignment.Importer {

  @Setter
  private String serviceUrl;

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private ObjectReferenceDto program;

  @Getter
  @Setter
  private ObjectReferenceDto facilityType;

  @Getter
  @Setter
  private Boolean hidden;

  @Getter
  @Setter
  private StockCardLineItemReason reason;

  @Override
  @JsonIgnore
  public UUID getProgramId() {
    return null == program ? null : program.getId();
  }

  @Override
  @JsonIgnore
  public void setProgramId(UUID programId) {
    this.program = new ObjectReferenceDto(serviceUrl, PROGRAMS, programId);
  }

  @Override
  @JsonIgnore
  public UUID getFacilityTypeId() {
    return null == facilityType ? null : facilityType.getId();
  }

  @Override
  @JsonIgnore
  public void setFacilityTypeId(UUID facilityTypeId) {
    this.facilityType = new ObjectReferenceDto(serviceUrl, FACILITY_TYPES, facilityTypeId);
  }

  /**
   * Creates new instance based on data from {@link ValidReasonAssignment}.
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
