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

import static java.util.UUID.fromString;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_NOT_EXIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH;

import java.util.Optional;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Component;

/**
 * This validator makes sure all lot ids included in stock event do exist in reference data.
 * And makes sure each lot id is associated with the orderable id that appears along its side.
 */
@Component(value = "LotValidator")
public class LotValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.info("validating lot");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    stockEventDto.getLineItems().forEach(lineItem -> {
      if (lineItem.hasLotId()) {
        LotDto lotDto = stockEventDto.getContext().findLot(lineItem.getLotId());
        checkLotExists(lineItem, lotDto);
        checkLotOrderableMatches(stockEventDto, lineItem, lotDto);
      }
    });
  }

  private void checkLotOrderableMatches(StockEventDto stockEventDto,
                                        StockEventLineItemDto lineItem, LotDto lotDto) {
    Optional<OrderableDto> foundOrderableDto = stockEventDto.getContext()
        .getAllApprovedProducts().stream()
        .filter(orderableDto -> orderableDto.getId().equals(lineItem.getOrderableId()))
        .findFirst();

    if (foundOrderableDto.isPresent()) {
      String tradeItemId = foundOrderableDto.get().getIdentifiers().get("tradeItem");
      if (tradeItemId == null || !lotDto.getTradeItemId().equals(fromString(tradeItemId))) {
        throw new ValidationMessageException(
            new Message(ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH,
                lineItem.getLotId(), lineItem.getOrderableId()));
      }
    }
  }

  private void checkLotExists(StockEventLineItemDto lineItem, LotDto lotDto) {
    if (lotDto == null) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_LOT_NOT_EXIST, lineItem.getLotId()));
    }
  }
}
