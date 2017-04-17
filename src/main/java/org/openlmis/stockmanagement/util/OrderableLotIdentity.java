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

package org.openlmis.stockmanagement.util;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderableLotIdentity {
  private UUID orderableId;
  private UUID lotId;

  public OrderableLotIdentity(UUID orderableId, UUID lotId) {
    this.orderableId = orderableId;
    this.lotId = lotId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrderableLotIdentity that = (OrderableLotIdentity) o;

    if (orderableId != null ? !orderableId.equals(that.orderableId) : that.orderableId != null)
      return false;
    return lotId != null ? lotId.equals(that.lotId) : that.lotId == null;
  }

  @Override
  public int hashCode() {
    int result = orderableId != null ? orderableId.hashCode() : 0;
    result = 31 * result + (lotId != null ? lotId.hashCode() : 0);
    return result;
  }
}
