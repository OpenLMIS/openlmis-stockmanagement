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

import static java.util.stream.Collectors.summingInt;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANNOT_UNPACK_CONSTITUENT_NOT_ACCOUNTED_FOR;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANNOT_UNPACK_REGULAR_ORDERABLE;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("UnpackKitValidator")
public class UnpackKitValidator implements StockEventValidator {

  @Autowired
  OrderableReferenceDataService orderableReferenceDataService;

  @Override
  public void validate(StockEventDto stockEventDto) {
    // do not try to validate this event if it is not kit unpacking event.
    if (!stockEventDto.isKitUnpacking()) {
      return;
    }

    StockEventProcessContext context = stockEventDto.getContext();
    List<UUID> orderableIds = stockEventDto
        .getLineItems()
        .stream()
        .map(StockEventLineItemDto::getOrderableId)
        .collect(Collectors.toList());
    Map<UUID, OrderableDto> orderables = orderableReferenceDataService
        .findByIds(orderableIds)
        .stream()
        .collect(Collectors.toMap(OrderableDto::getId, Function.identity()));

    Map<UUID, Integer> nonUnpackQuantities = stockEventDto
        .getLineItems()
        .stream()
        .filter(l -> !context.getUnpackReasonId().equals(l.getReasonId()))
        .collect(Collectors
            .groupingBy(
                StockEventLineItemDto::getOrderableId,
                summingInt(StockEventLineItemDto::getQuantity)));

    stockEventDto.getLineItems()
        .stream()
        .filter(item -> context.getUnpackReasonId().equals(item.getReasonId()))
        .forEach(line -> validateLineItem(line, orderables.get(line.getOrderableId()),
            nonUnpackQuantities));

  }

  private void validateLineItem(StockEventLineItemDto lineItem,
      @NotNull OrderableDto orderable, Map<UUID, Integer> orderableCredits) {

    // check if the orderable is a kit. if not throw exception.
    if (isEmpty(orderable.getChildren())) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_CANNOT_UNPACK_REGULAR_ORDERABLE, lineItem.getOrderableId()));
    }

    // check if the constituent products are all accounted for.
    orderable.getChildren().forEach(orderableChild -> {
      Integer quantityToAccountFor = lineItem.getQuantity() * orderableChild.getQuantity();
      Integer constituentCredits = orderableCredits.get(orderableChild.getOrderable().getId());
      if (constituentCredits == null || quantityToAccountFor > constituentCredits) {
        throw new ValidationMessageException(
            new Message(ERROR_EVENT_CANNOT_UNPACK_CONSTITUENT_NOT_ACCOUNTED_FOR,
                lineItem.getOrderableId()));
      } else {
        orderableCredits.replace(orderableChild.getOrderable().getId(),
            constituentCredits - quantityToAccountFor);
      }
    });

  }
}
