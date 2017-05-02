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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_IN_VALID_LIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_IN_VALID_LIST;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component(value = "SourceDestinationAssignmentValidator")
public class SourceDestinationAssignmentValidator implements StockEventValidator {

  @Autowired
  private ValidSourceAssignmentRepository validSourceAssignmentRepository;

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @Override
  public void validate(StockEventDto eventDto) {
    LOGGER.debug("Validate source and destination assignment");
    if (!eventDto.hasLineItems()) {
      return;
    }

    eventDto.getLineItems().forEach(eventLineItem -> {
      checkSourceDestinationBothPresent(eventLineItem);
      checkIsValidAssignment(eventLineItem, eventDto);
    });
  }

  private void checkIsValidAssignment(StockEventLineItem eventLineItem, StockEventDto eventDto) {
    UUID facilityTypeId = getFacilityTypeId(eventDto.getContext());
    UUID programId = eventDto.getProgramId();
    //this validator does not care if program missing or facility not found in ref data
    //that is handled in other validators
    if (facilityTypeId != null && programId != null) {
      if (eventLineItem.hasSource()) {
        checkSourceAssignment(eventLineItem, facilityTypeId, programId);
      }
      if (eventLineItem.hasDestination()) {
        checkDestinationAssignment(eventLineItem, facilityTypeId, programId);
      }
    }
  }

  private void checkSourceDestinationBothPresent(StockEventLineItem eventLineItem) {
    if (eventLineItem.hasSource() && eventLineItem.hasDestination()) {
      throwError(ERROR_SOURCE_DESTINATION_BOTH_PRESENT,
          eventLineItem.getSourceId(), eventLineItem.getDestinationId());
    }
  }

  private void checkSourceAssignment(StockEventLineItem eventLineItem,
                                     UUID facilityTypeId, UUID programId) {
    List<ValidSourceAssignment> sourceAssignments = validSourceAssignmentRepository
        .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    boolean isInValidList = sourceAssignments.stream().anyMatch(validSourceAssignment ->
        validSourceAssignment.getNode().getId().equals(eventLineItem.getSourceId()));
    if (!isInValidList) {
      throwError(ERROR_SOURCE_NOT_IN_VALID_LIST, eventLineItem.getSourceId());
    }
  }

  private void checkDestinationAssignment(StockEventLineItem eventLineItem,
                                          UUID facilityTypeId, UUID programId) {
    List<ValidDestinationAssignment> sourceAssignments = validDestinationAssignmentRepository
        .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    boolean isInValidList = sourceAssignments.stream().anyMatch(validSourceAssignment ->
        validSourceAssignment.getNode().getId().equals(eventLineItem.getDestinationId()));
    if (!isInValidList) {
      throwError(ERROR_DESTINATION_NOT_IN_VALID_LIST, eventLineItem.getDestinationId());
    }
  }

  private UUID getFacilityTypeId(StockEventProcessContext context) {
    FacilityDto facilityDto = context.getFacility();
    if (facilityDto != null) {
      return facilityDto.getType().getId();
    }
    return null;
  }

  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
