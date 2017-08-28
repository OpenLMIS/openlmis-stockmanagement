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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_FACILITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_DISCREPANCY_QUANTITY_NOT_PROVIDED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_PROVIDED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_VALID;

import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * This validator ensures that physical inventory line items
 * have stock adjustments with quantity and valid reasons.
 */

@Component(value = "DiscrepancyReasonsValidator")
public class DiscrepancyReasonsValidator implements StockEventValidator {

  @Autowired
  private ValidReasonAssignmentRepository validReasonRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {

    if (stockEventDto.isPhysicalInventory()) {
      stockEventDto.getLineItems().forEach(line ->
          line.getStockAdjustments().forEach(a -> {
            if (a.getQuantity() == null) {
              throw new ValidationMessageException(
                  new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_QUANTITY_NOT_PROVIDED));
            }
            StockCardLineItemReason reason = a.getReason();
            if (reason == null) {
              throw new ValidationMessageException(
                  new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_PROVIDED));
            }
            UUID reasonTypeId = getReasonTypeId(stockEventDto);
            UUID id = reason.getId();
            if (!isReasonValid(stockEventDto, id, reasonTypeId)) {
              throwException(stockEventDto, id, reasonTypeId);
            }
          })
      );
    }
  }

  private UUID getReasonTypeId(StockEventDto stockEventDto) {
    FacilityDto facility =
        facilityReferenceDataService.findOne(stockEventDto.getFacilityId());
    if (facility == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_FACILITY_INVALID, stockEventDto.getFacilityId()));
    }
    return facility.getType().getId();
  }

  private boolean isReasonValid(StockEventDto stockEventDto, UUID id, UUID facilityTypeId) {
    ValidReasonAssignment validReason =
        validReasonRepository.findByProgramIdAndFacilityTypeIdAndReasonId(
            stockEventDto.getProgramId(), facilityTypeId, id);
    return validReason != null;
  }

  private void throwException(StockEventDto stockEventDto, UUID id, UUID facilityTypeId) {
    throw new ValidationMessageException(
        new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_VALID,
            id, stockEventDto.getProgramId(), facilityTypeId));
  }
}
