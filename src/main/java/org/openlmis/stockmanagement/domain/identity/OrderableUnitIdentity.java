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

package org.openlmis.stockmanagement.domain.identity;

import static java.util.Optional.ofNullable;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.stockmanagement.dto.BaseDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableChildDto;
import org.openlmis.stockmanagement.dto.referencedata.UnitOfOrderableDto;

@Getter
@Setter
@EqualsAndHashCode
public class OrderableUnitIdentity {
  private final UUID orderableId;
  private final UUID unitOfOrderableId;

  /**
   * Create new instance from {@code lineItemDto}.
   *
   * @param lineItemDto the line item, not null
   */
  public OrderableUnitIdentity(StockEventLineItemDto lineItemDto) {
    this.orderableId = lineItemDto.getOrderableId();
    this.unitOfOrderableId = lineItemDto.getUnitOfOrderableId();
  }

  /**
   * Create new instance from {@code orderableChildDto}.
   *
   * @param orderableChildDto the line item, not null
   */
  public OrderableUnitIdentity(OrderableChildDto orderableChildDto) {
    this.orderableId =
        ofNullable(orderableChildDto.getOrderable()).map(BaseDto::getId).orElse(null);
    this.unitOfOrderableId =
        ofNullable(orderableChildDto.getUnit()).map(UnitOfOrderableDto::getId).orElse(null);
  }
}
