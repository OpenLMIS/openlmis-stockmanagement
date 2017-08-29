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

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
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
import java.util.List;
import java.util.UUID;

/**
 * This validator ensures that physical inventory line items
 * have stock adjustments with quantity and valid reasons.
 */

@Component(value = "DiscrepancyReasonsValidator")
public class PhysicalInventoryAdjustmentReasonsValidator implements StockEventValidator {

  @Autowired
  private ValidReasonAssignmentRepository validReasonRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {

    if (stockEventDto.isPhysicalInventory()) {
      stockEventDto.getLineItems().forEach(line -> validateAdjustments(stockEventDto, line)
      );
    }
  }

  private void validateAdjustments(StockEventDto event, StockEventLineItem line) {
    List<StockAdjustment> stockAdjustments = line.getStockAdjustments();
    if (stockAdjustments != null) {
      stockAdjustments.forEach(adjustment -> {
        validateQuantity(adjustment.getQuantity());
        validateReason(event.getProgramId(), event.getFacilityId(), adjustment.getReason());
      });
    }
  }

  private void validateQuantity(Integer quantity) {
    if (quantity == null) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_QUANTITY_NOT_PROVIDED));
    }
  }

  private void validateReason(UUID programId, UUID facilityId, StockCardLineItemReason reason) {
    if (reason == null) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_PROVIDED));
    }
    UUID facilityType = getFacilityType(facilityId);
    UUID reasonId = reason.getId();
    if (!isReasonValid(programId, facilityType, reasonId)) {
      throwException(programId, facilityType, reasonId);
    }
  }

  private UUID getFacilityType(UUID facilityId) {
    FacilityDto facility =
        facilityReferenceDataService.findOne(facilityId);
    if (facility == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_FACILITY_INVALID, facilityId));
    }
    return facility.getType().getId();
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
