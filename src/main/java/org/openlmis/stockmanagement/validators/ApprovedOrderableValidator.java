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
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_ORDERABLE_NOT_IN_APPROVED_LIST;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component(value = "ApprovedOrderableValidator")
public class ApprovedOrderableValidator implements StockEventValidator {

  /**
   * Validate if the orderable in stock event is in the approved list.
   *
   * @param stockEventDto the event to be validated.
   */
  //this validator does not care if facility or program or orderable are missing
  //that is other validator's job
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate approved product reference data service");
    UUID facility = stockEventDto.getFacilityId();
    UUID program = stockEventDto.getProgramId();
    if (!stockEventDto.hasLineItems() || facility == null || program == null) {
      return;
    }

    List<UUID> nonApprovedIds =
        findNonApprovedIds(stockEventDto, stockEventDto.getContext().getAllApprovedProducts());

    if (!isEmpty(nonApprovedIds)) {
      throw new ValidationMessageException(
          new Message(ERROR_ORDERABLE_NOT_IN_APPROVED_LIST, nonApprovedIds));
    }
  }

  private List<UUID> findNonApprovedIds(StockEventDto stockEventDto,
                                        Collection<OrderableDto> approvedProductDtos) {
    List<UUID> approvedIds = approvedProductDtos.stream()
        .map(OrderableDto::getId)
        .collect(toList());

    return stockEventDto.getLineItems().stream()
        .map(StockEventLineItem::getOrderableId)
        .filter(id -> !approvedIds.contains(id))
        .collect(toList());

  }
}
