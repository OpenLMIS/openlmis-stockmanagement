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

import java.util.List;
import org.openlmis.stockmanagement.domain.identity.OrderableLotUnitIdentity;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * When submitting a physical inventory, user MUST claim a stock on hand for each existing stock
 * card.
 * This validator makes sure no existing stock card is missing in the stock event.
 */

// TODO: Disabled because of OLMIS-2834 - we want to allow inventories from requisitions
// that do not fully cover the stock card for now.
//@Component(value = "ActiveStockCardsValidator")
public class ActiveStockCardsValidator implements StockEventValidator {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);
    Profiler profiler = new Profiler("ACTIVE_STOCK_CARDS_VALIDATOR");
    profiler.setLogger(XLOGGER);

    boolean noProgram = stockEventDto.getProgramId() == null;
    boolean noFacility = stockEventDto.getFacilityId() == null;
    boolean notPhysicalInventory = !stockEventDto.isPhysicalInventory();

    if (noProgram || noFacility || notPhysicalInventory) {
      return;//only need to do this validation for physical inventory
    }

    checkAllStockCardsCovered(stockEventDto, profiler.startNested("CHECK_STOCK_CARDS"));

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
  }

  private void checkAllStockCardsCovered(StockEventDto stockEventDto, Profiler profiler) {
    profiler.start("GET_ORDERABLE_LOT_IDENTITIES");
    List<OrderableLotUnitIdentity> coveredIdentities = stockEventDto.getLineItems().stream()
        .map(OrderableLotUnitIdentity::identityOf)
        .collect(toList());

    profiler.start("FIND_STOCK_CARDS_BY_PROGRAM_AND_FACILITY");
    boolean anyMissing = stockCardRepository
        .getIdentitiesBy(stockEventDto.getProgramId(), stockEventDto.getFacilityId())
        .stream().anyMatch(identity -> !coveredIdentities.contains(identity));

    if (anyMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD));
    }
  }

}
