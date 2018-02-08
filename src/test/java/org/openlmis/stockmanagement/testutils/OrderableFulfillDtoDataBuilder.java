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

package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderableFulfillDtoDataBuilder {

  private List<UUID> canFulfillForMe;
  private List<UUID> canBeFulfilledByMe;

  public OrderableFulfillDtoDataBuilder() {
    canFulfillForMe = new ArrayList<>();
    canBeFulfilledByMe = new ArrayList<>();
  }

  /**
   * Creates new instance of {@link OrderableFulfillDto} with properties.
   * @return created orderable fulfill dto
   */
  public OrderableFulfillDto build() {
    return new OrderableFulfillDto(canFulfillForMe, canBeFulfilledByMe);
  }

  public OrderableFulfillDtoDataBuilder withCanFulfillForMe(List<UUID> canFulfillForMe) {
    this.canFulfillForMe = canFulfillForMe;
    return this;
  }

  public OrderableFulfillDtoDataBuilder withCanBeFulfilledByMe(List<UUID> canBeFulfilledByMe) {
    this.canBeFulfilledByMe = canBeFulfilledByMe;
    return this;
  }
}
