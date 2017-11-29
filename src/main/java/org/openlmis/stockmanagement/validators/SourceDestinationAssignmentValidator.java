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

import org.openlmis.stockmanagement.domain.sourcedestination.SourceDestinationAssignment;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * This validator makes sure the sources/destinations in a stock event are in the valid lists.
 * Meaning that users can not just issue to or receive from anywhere they want. Users have to pick
 * that from a valid list that has been set up by admin or implementer.
 */
@Component(value = "SourceDestinationAssignmentValidator")
public class SourceDestinationAssignmentValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto eventDto) {
    LOGGER.debug("Validate source and destination assignment");
    if (!eventDto.hasLineItems()) {
      return;
    }

    UUID facilityTypeId = eventDto.getContext().getFacilityTypeId();
    UUID programId = eventDto.getProgramId();

    //this validator does not care if program missing or facility not found in ref data
    //that is handled in other validators
    if (null != facilityTypeId && null != programId) {
      eventDto
          .getLineItems()
          .forEach(this::checkSourceDestinationBothPresent);

      eventDto
          .getLineItems()
          .forEach(eventLineItem -> checkIsValidAssignment(eventDto, eventLineItem));
    }
  }

  private void checkIsValidAssignment(StockEventDto eventDto, StockEventLineItemDto eventLineItem) {
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
    boolean isInValidList = checkAssignment(context.getSources(), eventLineItem.getSourceId());

    if (!isInValidList) {
      throwError(ERROR_SOURCE_NOT_IN_VALID_LIST, eventLineItem.getSourceId());
    }
  }

  private void checkDestinationAssignment(StockEventProcessContext context,
                                          StockEventLineItemDto eventLineItem) {
    boolean isInValidList = checkAssignment(
        context.getDestinations(), eventLineItem.getDestinationId()
    );

    if (!isInValidList) {
      throwError(ERROR_DESTINATION_NOT_IN_VALID_LIST, eventLineItem.getDestinationId());
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
