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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_NOT_EXIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH;

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.stereotype.Component;

@Component(value = "LotValidator")
public class LotValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {
    LOGGER.info("validating lot");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    stockEventDto.getLineItems().forEach(lineItem -> {
      if (lineItem.hasLot()) {
        LotDto lotDto = stockEventDto.getContext().getLots().get(lineItem.getLotId());
        if (lotDto == null) {
          throw new ValidationMessageException(
              new Message(ERROR_EVENT_LOT_NOT_EXIST, lineItem.getLotId()));
        }
        if (!lotDto.getTradeItemId().equals(lineItem.getOrderableId())) {
          throw new ValidationMessageException(
              new Message(ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH,
                  lineItem.getLotId(), lineItem.getOrderableId()));
        }
      }
    });
  }
}
