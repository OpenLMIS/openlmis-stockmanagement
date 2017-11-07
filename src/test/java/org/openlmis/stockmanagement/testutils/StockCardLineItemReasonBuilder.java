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


import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;

public class StockCardLineItemReasonBuilder {
  /**
   * Create a stock card line item reason.
   *
   * @return created reason
   */
  public static StockCardLineItemReason createReason() {
    return createReason("Donation");
  }

  /**
   * Create a stock card line item reason with name.
   *
   * @param name reason name
   * @return created reason
   */
  public static StockCardLineItemReason createReason(String name) {
    StockCardLineItemReason reason = new StockCardLineItemReason();
    reason.setName(name);
    reason.setDescription("Donation from the donor");
    reason.setReasonType(ReasonType.CREDIT);
    reason.setReasonCategory(ReasonCategory.TRANSFER);
    reason.setIsFreeTextAllowed(true);
    return reason;
  }
}
