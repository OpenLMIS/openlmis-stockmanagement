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

package org.openlmis.stockmanagement.dto.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.dto.ValidReasonAssignmentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValidReasonAssignmentDtoBuilder {

  @Value("${service.url}")
  private String serviceUrl;

  /**
   * Create a new instance of {@link ValidReasonAssignmentDto} based on data
   * from {@link ValidReasonAssignment}.
   *
   * @param reasonAssignments list of {@link ValidReasonAssignment} used to create
   *                          {@link ValidReasonAssignmentDto}
   * @return new list of {@link ValidReasonAssignmentDto} or empty list if passed argument is
   *         {@code null}.
   */
  public List<ValidReasonAssignmentDto> build(List<ValidReasonAssignment> reasonAssignments) {
    if (null == reasonAssignments) {
      return Collections.emptyList();
    }

    List<ValidReasonAssignmentDto> dtos = new ArrayList<>();
    reasonAssignments.forEach(validReasonAssignment -> dtos.add(build(validReasonAssignment)));
    return dtos;
  }

  /**
   * Create a new instance of {@link ValidReasonAssignmentDto} based on data
   * from {@link ValidReasonAssignment}.
   *
   * @param reasonAssignment instance used to create {@link ValidReasonAssignmentDto}
   * @return new instance of {@link ValidReasonAssignmentDto}.
   *         {@code null} if passed argument is {@code null}.
   */
  public ValidReasonAssignmentDto build(ValidReasonAssignment reasonAssignment) {
    if (null == reasonAssignment) {
      return null;
    }
    return export(reasonAssignment);
  }

  private ValidReasonAssignmentDto export(ValidReasonAssignment reasonAssignment) {
    ValidReasonAssignmentDto dto = new ValidReasonAssignmentDto();
    dto.setId(reasonAssignment.getId());
    dto.setServiceUrl(serviceUrl);
    dto.setFacilityTypeId(reasonAssignment.getFacilityTypeId());
    dto.setProgramId(reasonAssignment.getProgramId());
    dto.setHidden(reasonAssignment.getHidden());
    dto.setReason(reasonAssignment.getReason() != null
        ? StockCardLineItemReasonDto.newInstance(reasonAssignment.getReason()) : null);
    return dto;
  }
}
