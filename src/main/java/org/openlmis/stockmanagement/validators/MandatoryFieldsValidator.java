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

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_FACILITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_NO_LINE_ITEMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_OCCURRED_DATE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_OCCURRED_DATE_IN_FUTURE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_PROGRAM_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_QUANTITIES_INVALID;

import java.time.LocalDate;
import java.util.List;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Component;

/**
 * This validator makes sure all must have fields are present.
 * Must have fields: facility id, program id, orderable id, occurred date, and quantity.
 */
@Component(value = "MandatoryFieldsValidator")
public class MandatoryFieldsValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate mandatory fields");
    validateFacilityProgramAndOrderables(stockEventDto);
    validateOccurredDate(stockEventDto);
    validateQuantity(stockEventDto);
  }

  private void validateFacilityProgramAndOrderables(StockEventDto dto) {
    if (dto.getContext().getFacility() == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_FACILITY_INVALID, dto.getFacilityId()));
    }

    if (dto.getContext().getProgram() == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_PROGRAM_INVALID, dto.getProgramId()));
    }

    if (!dto.hasLineItems()) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_NO_LINE_ITEMS, dto.getLineItems()));
    }

    boolean nullOrderableId = dto.getLineItems().stream()
        .anyMatch(lineItem -> lineItem.getOrderableId() == null);
    if (nullOrderableId) {
      throw new ValidationMessageException(ERROR_EVENT_ORDERABLE_INVALID);
    }
  }

  private void validateQuantity(StockEventDto stockEventDto) {
    List<Integer> invalidQuantities = stockEventDto.getLineItems().stream()
        .filter(lineItem -> lineItem.getQuantity() == null || lineItem.getQuantity() < 0)
        .map(StockEventLineItemDto::getQuantity)
        .collect(toList());

    if (!isEmpty(invalidQuantities)) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_QUANTITIES_INVALID, invalidQuantities));
    }
  }

  private void validateOccurredDate(StockEventDto stockEventDto) {
    if (stockEventDto.hasLineItems()) {
      stockEventDto.getLineItems().forEach(lineItem -> {
        LocalDate occurredDate = lineItem.getOccurredDate();
        if (occurredDate == null) {
          throw new ValidationMessageException(
              new Message(ERROR_EVENT_OCCURRED_DATE_INVALID, occurredDate));
        } else if (occurredDate.isAfter(LocalDate.now())) {
          //If the following error happens and the users have not chosen a future date in their
          //browser, ask them to visit "https://time.is", and see if there is a time drift on
          //the client machine. If so, ask them to re-sync their computer's time setting.
          throw new ValidationMessageException(
              new Message(ERROR_EVENT_OCCURRED_DATE_IN_FUTURE, occurredDate));
        }
      });
    }
  }
}
