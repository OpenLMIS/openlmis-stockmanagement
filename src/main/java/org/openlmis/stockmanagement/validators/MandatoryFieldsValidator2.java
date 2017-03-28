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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_PROGRAM_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_QUANTITY_INVALID;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component(value = "MandatoryFieldsValidator2")
public class MandatoryFieldsValidator2 implements StockEventValidator2 {

  @Override
  public void validate(StockEventDto2 stockEventDto) {
    LOGGER.debug("Validate mandatory fields");
    validateFacilityProgramAndOrderables(stockEventDto);
    validateOccurredDate(stockEventDto);
    validateQuantity(stockEventDto);
  }

  private void validateFacilityProgramAndOrderables(StockEventDto2 dto) {
    if (dto.getContext().getFacility() == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_FACILITY_INVALID, dto.getFacilityId()));
    }

    if (dto.getContext().getProgram() == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_PROGRAM_INVALID, dto.getProgramId()));
    }

    if (isEmpty(dto.getLineItems())) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_NO_LINE_ITEMS, dto.getLineItems()));
    }

    boolean nullOrderableId = dto.getLineItems().stream()
        .anyMatch(lineItem -> lineItem.getOrderableId() == null);
    if (nullOrderableId) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_ORDERABLE_INVALID, null));
    }
  }

  private void validateQuantity(StockEventDto2 stockEventDto) {
    List<StockEventLineItem> invalidQuantities = stockEventDto.getLineItems().stream()
        .filter(q -> q.getQuantity() == null || q.getQuantity() < 0)
        .collect(toList());

    if (!isEmpty(invalidQuantities)) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_QUANTITY_INVALID, invalidQuantities));
    }
  }

  private void validateOccurredDate(StockEventDto2 stockEventDto) {
    ZonedDateTime occurredDate = stockEventDto.getOccurredDate();
    if (occurredDate == null || occurredDate.isAfter(ZonedDateTime.now())) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_OCCURRED_DATE_INVALID, occurredDate));
    }
  }
}
