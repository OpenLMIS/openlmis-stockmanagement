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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_REASON_NOT_IN_VALID_LIST;

import org.openlmis.stockmanagement.domain.adjustment.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component(value = "ReasonAssignmentValidator")
public class ReasonAssignmentValidator implements StockEventValidator {

  @Autowired
  private ValidReasonAssignmentRepository validReasonAssignmentRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate reason assignment");
    FacilityDto facility = facilityReferenceDataService.findOne(stockEventDto.getFacilityId());
    UUID programId = stockEventDto.getProgramId();
    if (!stockEventDto.hasReason() || facility == null || programId == null) {
      return;
    }

    checkIsReasonInValidAssignmentList(stockEventDto, facility, programId);
  }

  private void checkIsReasonInValidAssignmentList(StockEventDto stockEventDto,
                                                  FacilityDto facility, UUID programId) {
    List<ValidReasonAssignment> validReasonAssignments = validReasonAssignmentRepository
        .findByProgramIdAndFacilityTypeId(programId, facility.getType().getId());

    boolean isInValidAssignmentList = validReasonAssignments.stream()
        .anyMatch(assignment ->
            assignment.getReason().getId().equals(stockEventDto.getReasonId()));

    if (!isInValidAssignmentList) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_REASON_NOT_IN_VALID_LIST, stockEventDto.getReasonId()));
    }
  }
}
