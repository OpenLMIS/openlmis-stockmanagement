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

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;

import java.util.List;
import java.util.Map;
import org.openlmis.stockmanagement.domain.identity.OrderableLotUnitIdentity;
import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

/**
 * 1 This validator makes sure stock on hand does NOT go below zero for any stock card. 2 This
 * validator also makes sure soh does not be over upper limit of integer. It does so by
 * re-calculating soh of each orderable/lot combo. The re-calculation does not apply to physical
 * inventory. This has a negative impact on performance. The impact grows larger as stock card line
 * items accumulates over time. Because re-calculation requires reading stock card line items from
 * DB.
 */
@Component(value = "QuantityValidator")
public class QuantityValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);
    Profiler profiler = new Profiler("QUANTITY_VALIDATOR");
    profiler.setLogger(XLOGGER);

    if (!stockEventDto.hasLineItems()) {
      return;
    }

    profiler.start("GET_ORDERABLE_GROUPS");
    Map<OrderableLotUnitIdentity, List<StockEventLineItemDto>> sameOrderableGroups = stockEventDto
        .getLineItems()
        .stream()
        .collect(groupingBy(OrderableLotUnitIdentity::identityOf));

    for (List<StockEventLineItemDto> group : sameOrderableGroups.values()) {
      // increase may cause int overflow, decrease may cause below zero
      validateEventItems(stockEventDto, group, profiler.startNested("VALIDATE_EVENT_LINE_ITEMS"));
    }

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
  }

  private void validateEventItems(StockEventDto event, List<StockEventLineItemDto> items,
      Profiler profiler) {
    if (event.isPhysicalInventory()) {
      profiler.start("VALIDATE_PHYSICAL_INVENTORY_QUANTITIES");
      validateQuantities(items);
    }
  }

  private void validateQuantities(List<StockEventLineItemDto> items) {
    for (StockEventLineItemDto item : items) {
      Integer quantity = item.getQuantity();
      if (quantity != null) {
        List<StockEventAdjustmentDto> adjustments = item.getStockAdjustments();
        if (isNotEmpty(adjustments)) {
          validateStockAdjustments(adjustments);
        }
      }
    }
  }

  /**
   * Make sure each stock adjustment a non-negative quantity assigned.
   *
   * @param adjustments adjustments to validate
   */
  private void validateStockAdjustments(List<StockEventAdjustmentDto> adjustments) {
    // Check for valid quantities
    boolean hasNegative = adjustments
        .stream()
        .mapToInt(StockEventAdjustmentDto::getQuantity)
        .anyMatch(quantity -> quantity < 0);

    if (hasNegative) {
      throw new ValidationMessageException(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);
    }
  }
}