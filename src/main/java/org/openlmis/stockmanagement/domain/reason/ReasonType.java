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

package org.openlmis.stockmanagement.domain.reason;

import lombok.Getter;

public enum ReasonType {
  CREDIT(2),
  DEBIT(1),
  BALANCE_ADJUSTMENT(0);

  /**
   * Value of this field will be used to set correct order of stock card line items if both
   * occurred and processed dates have the same date for the following line items. It is
   * important that types that are used to increase (like {@link #CREDIT}) should have higher
   * priority than types that are used to decrease (like {@link #DEBIT}).
   */
  @Getter
  private int priority;

  ReasonType(int priority) {
    this.priority = priority;
  }
}
