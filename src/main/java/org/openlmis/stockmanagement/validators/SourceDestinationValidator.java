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

import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_IN_VALID_LIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_IN_VALID_LIST;

@Component(value = "SourceDestinationValidator")
public class SourceDestinationValidator implements StockEventValidator {

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private ValidSourceAssignmentRepository validSourceAssignmentRepository;

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @Override
  public void validate(StockEventDto eventDto) {
    checkSourceDestinationBothPresent(eventDto);
    if (eventDto.hasSource()) {
      checkSourceInValidList(eventDto);
    }
    if (eventDto.hasDestination()) {
      checkDestinationInValidList(eventDto);
    }
  }

  private void checkSourceDestinationBothPresent(StockEventDto eventDto) {
    if (eventDto.hasSource() && eventDto.hasDestination()) {
      throwError(ERROR_SOURCE_DESTINATION_BOTH_PRESENT,
              eventDto.getSourceId(), eventDto.getDestinationId());
    }
  }

  private void checkSourceInValidList(StockEventDto eventDto) {
    UUID facilityTypeId = getFacilityTypeId(eventDto);
    UUID programId = eventDto.getProgramId();

    List<ValidSourceAssignment> sourceAssignments = validSourceAssignmentRepository
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    boolean isInValidList = sourceAssignments.stream().anyMatch(validSourceAssignment ->
            validSourceAssignment.getNode().getId().equals(eventDto.getSourceId()));
    if (!isInValidList) {
      throwError(ERROR_SOURCE_NOT_IN_VALID_LIST, eventDto.getSourceId());
    }
  }

  private void checkDestinationInValidList(StockEventDto eventDto) {
    UUID facilityTypeId = getFacilityTypeId(eventDto);
    UUID programId = eventDto.getProgramId();

    List<ValidDestinationAssignment> sourceAssignments = validDestinationAssignmentRepository
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    boolean isInValidList = sourceAssignments.stream().anyMatch(validSourceAssignment ->
            validSourceAssignment.getNode().getId().equals(eventDto.getDestinationId()));
    if (!isInValidList) {
      throwError(ERROR_DESTINATION_NOT_IN_VALID_LIST, eventDto.getDestinationId());
    }
  }

  private UUID getFacilityTypeId(StockEventDto eventDto) {
    FacilityDto facilityDto = facilityReferenceDataService.findOne(eventDto.getFacilityId());
    return facilityDto.getType().getId();
  }

  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
