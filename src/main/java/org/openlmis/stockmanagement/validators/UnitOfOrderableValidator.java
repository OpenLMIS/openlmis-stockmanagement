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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_UNIT_OF_ORDERABLE_DOES_NOT_EXIST;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.UnitOfOrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.UnitOfOrderableReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * This validator checks if unit of orderable with given UUID exists.
 */
@Component("UnitOfOrderableValidator")
public class UnitOfOrderableValidator implements StockEventValidator {

  @Autowired
  private UnitOfOrderableReferenceDataService unitOfOrderableReferenceDataService;

  @Override
  public void validate(StockEventDto stockEventDto) {

    List<StockEventLineItemDto> lineItems = stockEventDto.getLineItems();
    if (CollectionUtils.isEmpty(lineItems)) {
      return;
    }
    for (StockEventLineItemDto lineItemDto : lineItems) {

      if (lineItemDto == null) {
        continue;
      }
      UUID unitOfOrderableId = lineItemDto.getUnitOfOrderableId();
      if (unitOfOrderableId == null) {
        continue;
      }

      UnitOfOrderableDto unitOfOrderableDto =
          unitOfOrderableReferenceDataService.findOne(unitOfOrderableId);
      if (unitOfOrderableDto == null) {
        throw new ValidationMessageException(
            new Message(
                ERROR_LINE_ITEM_UNIT_OF_ORDERABLE_DOES_NOT_EXIST,
                unitOfOrderableId,
                lineItemDto.getOrderableId(),
                lineItemDto.getLotId()
            )
        );
      }
    }

  }
}
