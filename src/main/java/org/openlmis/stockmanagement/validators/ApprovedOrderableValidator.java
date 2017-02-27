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

import static java.util.stream.StreamSupport.stream;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_ORDERABLE_NOT_IN_APPROVED_LIST;

import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component(value = "ApprovedOrderableValidator")
public class ApprovedOrderableValidator implements StockEventValidator {

  @Autowired
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  /**
   * Validate if the orderable in stock event is in the approved list.
   *
   * @param stockEventDto the event to be validated.
   */
  //this validator does not care if facility or program or orderable are missing
  //that is other validator's job
  public void validate(StockEventDto stockEventDto) {
    UUID facility = stockEventDto.getFacilityId();
    UUID program = stockEventDto.getProgramId();
    if (stockEventDto.getOrderableId() == null || facility == null || program == null) {
      return;
    }

    boolean isFoundInApprovedList = tryToFindInApprovedList(stockEventDto,
        approvedProductReferenceDataService.getAllApprovedProducts(program, facility));

    if (!isFoundInApprovedList) {
      throw new ValidationMessageException(
          new Message(ERROR_ORDERABLE_NOT_IN_APPROVED_LIST, stockEventDto.getOrderableId()));
    }
  }

  private boolean tryToFindInApprovedList(StockEventDto stockEventDto,
                                          Collection<ApprovedProductDto> approvedProductDtos) {
    return stream(approvedProductDtos.spliterator(), false)
        .anyMatch(product -> {
          UUID orderableId = product.getProgramOrderable().getOrderableId();
          return orderableId.equals(stockEventDto.getOrderableId());
        });
  }

  private Collection<ApprovedProductDto> getApprovedList(
      UUID facilityId, UUID programId, boolean fullSupply) {
    return approvedProductReferenceDataService
        .getApprovedProducts(facilityId, programId, fullSupply);
  }
}
