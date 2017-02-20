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

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.BaseReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_FACILITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_PROGRAM_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_OCCURRED_DATE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_QUANTITY_INVALID;

@Component(value = "MandatoryFieldsValidator")
public class MandatoryFieldsValidator implements StockEventValidator {

  @Autowired
  FacilityReferenceDataService facilityRefDataService;

  @Autowired
  ProgramReferenceDataService programRefDataService;

  @Autowired
  OrderableReferenceDataService orderableRefDataService;

  @Override
  public void validate(StockEventDto stockEventDto) {
    validateFacilityProgramAndOrderable(stockEventDto);
    validateOccurredDate(stockEventDto);
    validateQuantity(stockEventDto);
  }

  private void validateFacilityProgramAndOrderable(StockEventDto stockEventDto) {
    validateByRefDataService(
        stockEventDto.getFacilityId(), facilityRefDataService, ERROR_EVENT_FACILITY_INVALID);

    validateByRefDataService(
        stockEventDto.getProgramId(), programRefDataService, ERROR_EVENT_PROGRAM_INVALID);

    validateByRefDataService(
        stockEventDto.getOrderableId(), orderableRefDataService, ERROR_EVENT_ORDERABLE_INVALID);
  }

  private void validateByRefDataService(
      UUID id, BaseReferenceDataService refDataService, String key) {
    if (id == null || refDataService.findOne(id) == null) {
      throw new ValidationMessageException(new Message(key, id));
    }
  }

  private void validateQuantity(StockEventDto stockEventDto) {
    Integer quantity = stockEventDto.getQuantity();
    if (quantity == null || quantity < 0) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_QUANTITY_INVALID, quantity));
    }
  }

  private void validateOccurredDate(StockEventDto stockEventDto) {
    ZonedDateTime occurredDate = stockEventDto.getOccurredDate();
    if (occurredDate == null || occurredDate.isAfter(ZonedDateTime.now())) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_OCCURRED_DATE_INVALID, occurredDate));
    }
  }
}
