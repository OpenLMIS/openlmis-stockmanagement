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

import static java.util.stream.Collectors.toSet;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_LOT_DUPLICATION;

import java.util.HashSet;
import java.util.Set;
import org.openlmis.stockmanagement.domain.identity.OrderableLotUnitIdentity;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

/**
 * This validator is for physical inventory only.
 * In a physical inventory, a user is only allowed to claim stock on hand ONCE for each
 * orderable/lot combo.
 * Such rule is NOT applied to adjustment, issue, receive.
 */
@Component(value = "OrderableLotUnitDuplicationValidator")
public class OrderableLotUnitDuplicationValidator implements StockEventValidator {
  @Override
  public void validate(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);
    Profiler profiler = new Profiler("ORDERABLE_LOT_DUPLICATION_VALIDATOR");
    profiler.setLogger(XLOGGER);

    //duplication is not allow in physical inventory, but is allowed in adjustment
    if (!stockEventDto.hasLineItems() || !stockEventDto.isPhysicalInventory()) {
      return;
    }

    Set<OrderableLotUnitIdentity> nonDuplicates = new HashSet<>();

    profiler.start("GET_DUPLICATES");
    Set<OrderableLotUnitIdentity> duplicates = stockEventDto.getLineItems()
        .stream().map(OrderableLotUnitIdentity::identityOf)
        .filter(orderableLotUnitIdentity -> !nonDuplicates.add(orderableLotUnitIdentity))
        .collect(toSet());

    if (duplicates.size() > 0) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_ORDERABLE_LOT_DUPLICATION, duplicates));
    }

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
  }
}
