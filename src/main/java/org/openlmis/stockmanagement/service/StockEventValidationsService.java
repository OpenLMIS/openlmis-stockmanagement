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

package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.extension.ExtensionManager;
import org.openlmis.stockmanagement.extension.point.AdjustmentReasonValidator;
import org.openlmis.stockmanagement.extension.point.ExtensionPointId;
import org.openlmis.stockmanagement.extension.point.FreeTextValidator;
import org.openlmis.stockmanagement.extension.point.UnpackKitValidator;
import org.openlmis.stockmanagement.validators.ApprovedOrderableValidator;
import org.openlmis.stockmanagement.validators.FacilityValidator;
import org.openlmis.stockmanagement.validators.LotValidator;
import org.openlmis.stockmanagement.validators.MandatoryFieldsValidator;
import org.openlmis.stockmanagement.validators.OrderableLotUnitDuplicationValidator;
import org.openlmis.stockmanagement.validators.PhysicalInventoryAdjustmentReasonsValidator;
import org.openlmis.stockmanagement.validators.QuantityValidator;
import org.openlmis.stockmanagement.validators.ReasonExistenceValidator;
import org.openlmis.stockmanagement.validators.ReceiveIssueReasonValidator;
import org.openlmis.stockmanagement.validators.SourceDestinationAssignmentValidator;
import org.openlmis.stockmanagement.validators.SourceDestinationGeoLevelAffinityValidator;
import org.openlmis.stockmanagement.validators.StockEventVvmValidator;
import org.openlmis.stockmanagement.validators.UnitOfOrderableValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An aggregator of all stock event validators.
 * All validators will run before any actual DB writing happens.
 * It any one validator detects something wrong, we'll stop processing the stock event.
 */
@Service
public class StockEventValidationsService {

  @Autowired
  private ApprovedOrderableValidator approvedOrderableValidator;

  @Autowired
  private LotValidator lotValidator;

  @Autowired
  private MandatoryFieldsValidator mandatoryFieldsValidator;

  @Autowired
  private OrderableLotUnitDuplicationValidator orderableLotUnitDuplicationValidator;

  @Autowired
  private PhysicalInventoryAdjustmentReasonsValidator physicalInventoryAdjustmentReasonsValidator;

  @Autowired
  private QuantityValidator quantityValidator;

  @Autowired
  private ReasonExistenceValidator existenceValidator;

  @Autowired
  private ReceiveIssueReasonValidator receiveIssueReasonValidator;

  @Autowired
  private SourceDestinationAssignmentValidator destinationAssignmentValidator;

  @Autowired
  private SourceDestinationGeoLevelAffinityValidator destinationGeoLevelAffinityValidator;

  @Autowired
  private StockEventVvmValidator stockEventVvmValidator;

  @Autowired
  private ExtensionManager extensionManager;

  @Autowired
  private UnitOfOrderableValidator unitOfOrderableValidator;

  @Autowired
  private FacilityValidator facilityValidator;

  /**
   * Validate stock event with permission service and all validators.
   *
   * @param stockEventDto the event to be validated.
   */
  public void validate(StockEventDto stockEventDto) {
    approvedOrderableValidator.validate(stockEventDto);
    lotValidator.validate(stockEventDto);
    mandatoryFieldsValidator.validate(stockEventDto);
    orderableLotUnitDuplicationValidator.validate(stockEventDto);
    physicalInventoryAdjustmentReasonsValidator.validate(stockEventDto);
    quantityValidator.validate(stockEventDto);
    existenceValidator.validate(stockEventDto);
    receiveIssueReasonValidator.validate(stockEventDto);
    destinationAssignmentValidator.validate(stockEventDto);
    destinationGeoLevelAffinityValidator.validate(stockEventDto);
    stockEventVvmValidator.validate(stockEventDto);
    unitOfOrderableValidator.validate(stockEventDto);
    facilityValidator.validate(stockEventDto);

    AdjustmentReasonValidator adjustmentReasonValidator = extensionManager.getExtension(
        ExtensionPointId.ADJUSTMENT_REASON_POINT_ID, AdjustmentReasonValidator.class);
    FreeTextValidator freeTextValidator = extensionManager.getExtension(
        ExtensionPointId.FREE_TEXT_POINT_ID, FreeTextValidator.class);
    UnpackKitValidator unpackKitValidator = extensionManager.getExtension(
        ExtensionPointId.UNPACK_KIT_POINT_ID, UnpackKitValidator.class);

    adjustmentReasonValidator.validate(stockEventDto);
    freeTextValidator.validate(stockEventDto);
    unpackKitValidator.validate(stockEventDto);
  }

}
