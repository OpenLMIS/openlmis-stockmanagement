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

import static java.util.stream.Collectors.toList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.util.OrderableLotIdentity;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "ActiveStockCardsValidator")
public class ActiveStockCardsValidator implements StockEventValidator {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto stockEventDto)
      throws IllegalAccessException, InstantiationException {
    if (stockEventDto.getProgramId() == null || stockEventDto.getFacilityId() == null) {
      return;
    }

    if (stockEventDto.isPhysicalInventory()) {
      checkAllStockCardsCovered(stockEventDto);
    }
  }

  private void checkAllStockCardsCovered(StockEventDto stockEventDto) {
    List<OrderableLotIdentity> coveredIdentities = stockEventDto.getLineItems().stream()
        .map(StockEventLineItem::orderableAndLotIdentity)
        .collect(toList());

    boolean anyMissing = stockCardRepository
        .getIdentitiesBy(stockEventDto.getProgramId(), stockEventDto.getFacilityId())
        .stream().anyMatch(identity -> !coveredIdentities.contains(identity));

    if (anyMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD));
    }
  }

}
