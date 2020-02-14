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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_REASON_NOT_EXIST;

import java.util.UUID;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

@Component(value = "ReasonExistenceValidator")
//this validator used to check if reason is in valid list of program&facility type
//but that rule has been removed when stock adjustment's UI is implemented
//if that rule comes back, you can find "ReasonAssignmentValidator.java" in commit history
public class ReasonExistenceValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);
    Profiler profiler = new Profiler("REASON_EXISTENCE_VALIDATOR");
    profiler.setLogger(XLOGGER);

    if (!stockEventDto.hasLineItems()) {
      return;
    }

    for (UUID reasonId : stockEventDto.getReasonIds()) {
      StockCardLineItemReason foundReason = stockEventDto.getContext().findEventReason(reasonId);

      if (foundReason == null) {
        throw new ValidationMessageException(
            new Message(ERROR_EVENT_REASON_NOT_EXIST, reasonId));
      }
    }

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
  }

}
