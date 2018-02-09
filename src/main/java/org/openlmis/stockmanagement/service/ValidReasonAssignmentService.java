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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_TYPE_INVALID;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ValidReasonAssignmentService {

  @Autowired
  private ValidReasonAssignmentRepository validReasonAssignmentRepository;

  @Autowired
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  /**
   * Find valid reason assignments by program ID, facility type ID and reason types.
   *
   * @param programId       program ID
   * @param facilityTypeId  facility type ID
   * @param reasonTypes     reason types' list
   * @return A list of valid reason assignments.
   */
  public List<ValidReasonAssignment> search(UUID programId, UUID facilityTypeId,
      List<String> reasonTypes) {

    List<ValidReasonAssignment> validReasonAssignments = Lists.newArrayList();
    List<StockCardLineItemReason> stockCardLineItemReasons = Lists.newArrayList();

    if (!CollectionUtils.isEmpty(reasonTypes)) {
      stockCardLineItemReasons =
          stockCardLineItemReasonRepository.findByReasonTypeIn(
              reasonTypes.stream().map(this::toEnum).collect(Collectors.toSet()));
    } else {
      validReasonAssignments = validReasonAssignmentRepository
          .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    }

    if (!CollectionUtils.isEmpty(stockCardLineItemReasons)) {
      validReasonAssignments = validReasonAssignmentRepository
          .findByProgramIdAndFacilityTypeIdAndReasonIn(programId, facilityTypeId,
              stockCardLineItemReasons);
    }
    return validReasonAssignments;
  }

  private ReasonType toEnum(String type) {
    ReasonType reasonType = ReasonType.fromString(type);

    if (null == reasonType) {
      throw new ValidationMessageException(new Message(ERROR_REASON_TYPE_INVALID, type));
    }

    return reasonType;
  }

}
