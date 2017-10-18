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

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(value = "ReasonExistenceValidator")
//this validator used to check if reason is in valid list of program&facility type
//but that rule has been removed when stock adjustment's UI is implemented
//if that rule comes back, you can find "ReasonAssignmentValidator.java" in commit history
public class ReasonExistenceValidator implements StockEventValidator {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepo;

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate reason existence");
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    List<StockEventLineItem> lineItems = stockEventDto.getLineItems();
    Set<UUID> reasonIds = lineItems
        .stream()
        .filter(StockEventLineItem::hasReasonId)
        .map(StockEventLineItem::getReasonId)
        .collect(Collectors.toSet());

    Map<UUID, StockCardLineItemReason> reasons = reasonRepo
        .findByIdIn(reasonIds)
        .stream()
        .collect(Collectors.toMap(StockCardLineItemReason::getId, reason -> reason));

    for (UUID reasonId : reasonIds) {
      StockCardLineItemReason foundReason = reasons.get(reasonId);

      if (foundReason == null) {
        throw new ValidationMessageException(
            new Message(ERROR_EVENT_REASON_NOT_EXIST, reasonId));
      }
    }
  }

}
