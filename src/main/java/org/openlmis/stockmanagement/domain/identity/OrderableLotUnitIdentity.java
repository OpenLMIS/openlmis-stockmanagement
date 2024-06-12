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

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Class acting as identity where triple (orderableId, lotId, unitOfOrderableId) is unique.
 */
@Getter
@Setter
public class OrderableLotUnitIdentity {
  private UUID orderableId;
  private UUID lotId;
  private UUID unitOfOrderableId;

  /**
   * Constructor for OrderableLotUnitIdentity.
   *
   * @param orderableId       orderable id
   * @param lotId             lot id
   * @param unitOfOrderableId unit of orderable id
   */
  public OrderableLotUnitIdentity(UUID orderableId, UUID lotId, UUID unitOfOrderableId) {
    this.orderableId = orderableId;
    this.lotId = lotId;
    this.unitOfOrderableId = unitOfOrderableId;
  }

  public static OrderableLotUnitIdentity identityOf(IdentifiableByOrderableLotUnit identifiable) {
    return new OrderableLotUnitIdentity(identifiable.getOrderableId(), identifiable.getLotId(),
        identifiable.getUnitOfOrderableId());
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    OrderableLotUnitIdentity that = (OrderableLotUnitIdentity) object;

    if (orderableId != null ? !orderableId.equals(that.orderableId) : that.orderableId != null) {
      return false;
    }
    if (lotId != null ? !lotId.equals(that.lotId) : that.lotId != null) {
      return false;
    }
    return unitOfOrderableId == null ? that.unitOfOrderableId == null :
        unitOfOrderableId.equals(that.unitOfOrderableId);
  }

  @Override
  public int hashCode() {
    int result = orderableId != null ? orderableId.hashCode() : 0;
    result = 31 * result + (lotId != null ? lotId.hashCode() : 0);
    result = 31 * result + (unitOfOrderableId != null ? unitOfOrderableId.hashCode() : 0);
    return result;
  }
}
