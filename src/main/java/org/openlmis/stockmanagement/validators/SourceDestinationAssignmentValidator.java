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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_FOUND;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.sourcedestination.SourceDestinationAssignment;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

/**
 * This validator checks if the both source and destination are not present at the same time
 * It also checks if given source or destination already exists.
 * Validation if chosen source or destination is covered by another validator
 */
@Component(value = "SourceDestinationAssignmentValidator")
public class SourceDestinationAssignmentValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto eventDto) {
    XLOGGER.entry(eventDto);
    Profiler profiler = new Profiler("SOURCE_DESTINATION_ASSIGNMENT_VALIDATOR");
    profiler.setLogger(XLOGGER);

    if (!eventDto.hasLineItems()) {
      return;
    }

    UUID facilityTypeId = eventDto.getContext().getFacilityTypeId();
    UUID programId = eventDto.getProgramId();

    //this validator does not care if program missing or facility not found in ref data
    //that is handled in other validators
    profiler.start("CHECK_SOURCE_AND_DESTINATION_ASSIGNMENTS");
    if (null != facilityTypeId && null != programId) {
      eventDto
          .getLineItems()
          .forEach(this::checkSourceDestinationBothPresent);

      eventDto
          .getLineItems()
          .forEach(eventLineItem -> checkExistingAssignment(eventDto, eventLineItem));
    }

    profiler.stop().log();
    XLOGGER.exit(eventDto);
  }

  private void checkExistingAssignment(StockEventDto eventDto,
      StockEventLineItemDto eventLineItem) {
    if (eventLineItem.hasSourceId()) {
      checkSourceAssignment(eventDto.getContext(), eventLineItem);
    }

    if (eventLineItem.hasDestinationId()) {
      checkDestinationAssignment(eventDto.getContext(), eventLineItem);
    }
  }

  private void checkSourceDestinationBothPresent(StockEventLineItemDto eventLineItem) {
    if (eventLineItem.hasSourceId() && eventLineItem.hasDestinationId()) {
      throwError(ERROR_SOURCE_DESTINATION_BOTH_PRESENT,
          eventLineItem.getSourceId(), eventLineItem.getDestinationId());
    }
  }

  private void checkSourceAssignment(StockEventProcessContext context,
                                     StockEventLineItemDto eventLineItem) {
    boolean exists = checkAssignment(context.getSources(), eventLineItem.getSourceId());

    if (!exists) {
      throwError(ERROR_SOURCE_NOT_FOUND, eventLineItem.getSourceId());
    }
  }

  private void checkDestinationAssignment(StockEventProcessContext context,
                                          StockEventLineItemDto eventLineItem) {
    boolean exists = checkAssignment(
        context.getDestinations(), eventLineItem.getDestinationId()
    );

    if (!exists) {
      throwError(ERROR_DESTINATION_NOT_FOUND, eventLineItem.getDestinationId());
    }
  }

  private boolean checkAssignment(List<? extends SourceDestinationAssignment> assignments,
                                  UUID nodeId) {
    return assignments
        .stream()
        .anyMatch(assignment -> assignment.getNode().getId().equals(nodeId));
  }


  private void throwError(String key, Object... params) {
    throw new ValidationMessageException(new Message(key, params));
  }
}
