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

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This validator ensures that physical inventory line items
 * have stock adjustments with quantity and valid reasons.
 */

@Component(value = "PhysicalInventoryAdjustmentReasonsValidator")
public class PhysicalInventoryAdjustmentReasonsValidator implements StockEventValidator {

  @Autowired
  private ValidReasonAssignmentRepository validReasonRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {

    if (stockEventDto.isPhysicalInventory()) {
      for (StockEventLineItemDto line : stockEventDto.getLineItems()) {
        validateAdjustments(stockEventDto, line);
      }
    }
  }

  private void validateAdjustments(StockEventDto event, StockEventLineItemDto line) {
    List<StockEventAdjustmentDto> stockAdjustments = line.getStockAdjustments();

    if (stockAdjustments != null) {
      for (StockEventAdjustmentDto adjustment : stockAdjustments) {
        validateQuantity(adjustment.getQuantity());
        validateReason(event, adjustment.getReasonId());
      }
    }
  }

  private void validateQuantity(Integer quantity) {
    if (quantity == null) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_QUANTITY_NOT_PROVIDED));
    }
  }

  private void validateReason(StockEventDto event, UUID reasonId) {
    if (reasonId == null) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_PROVIDED));
    }
    UUID facilityType = getFacilityType(event);

    UUID programId = event.getProgramId();
    if (!isReasonValid(programId, facilityType, reasonId)) {
      throwException(programId, facilityType, reasonId);
    }
  }

  private UUID getFacilityType(StockEventDto event) {
    UUID typeId = event.getContext().getFacilityTypeId();

    if (null == typeId) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_FACILITY_INVALID, event.getFacilityId()));
    }

    return typeId;
  }

  private boolean isReasonValid(UUID programId, UUID facilityTypeId, UUID reasonId) {
    ValidReasonAssignment validReason =
        validReasonRepository.findByProgramIdAndFacilityTypeIdAndReasonId(
            programId, facilityTypeId, reasonId);
    return validReason != null;
  }

  private void throwException(UUID programId, UUID facilityTypeId, UUID reasonId) {
    throw new ValidationMessageException(
        new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_VALID,
            reasonId, programId, facilityTypeId));
  }
}
