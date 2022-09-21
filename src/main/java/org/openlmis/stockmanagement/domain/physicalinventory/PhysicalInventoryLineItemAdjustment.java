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

package org.openlmis.stockmanagement.domain.physicalinventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;

@Data
@Builder
@Entity
@Table(name = "physical_inventory_line_item_adjustments")
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryLineItemAdjustment extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "reasonId", nullable = false)
  private StockCardLineItemReason reason;

  @Column(nullable = false)
  private Integer quantity;

  /**
   * Returns quantity value with correct sign depending on reason type.
   *
   * @return quantity value, is negative for Debit reason
   */
  public Integer getQuantityWithSign() {
    if (reason != null && reason.getReasonType() == ReasonType.DEBIT) {
      return this.getQuantity() * -1;
    }
    return this.getQuantity();
  }
}
