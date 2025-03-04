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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.physicalinventory.PhysicalInventoryLineItemAdjustmentDto;

@Data
@Builder
@Entity
@Table(name = "physical_inventory_line_item_adjustments")
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryLineItemAdjustment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stockeventlineitemid")
  private StockEventLineItem stockEventLineItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stockcardlineitemid")
  private StockCardLineItem stockCardLineItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "physicalinventorylineitemid")
  private PhysicalInventoryLineItem physicalInventoryLineItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reasonId", nullable = false)
  private StockCardLineItemReason reason;

  @Column(nullable = false)
  private Integer quantity;

  /**
   * Create new instance of PhysicalInventoryLineItemAdjustment for StockEventLineItem.
   *
   * @param parent the parent
   * @param dto the dto
   * @return new instance never null
   */
  public static PhysicalInventoryLineItemAdjustment newInstance(StockEventLineItem parent,
      PhysicalInventoryLineItemAdjustmentDto dto) {
    return new PhysicalInventoryLineItemAdjustment(parent, null, null, dto.getReason(),
        dto.getQuantity());
  }

  /**
   * Create new instance of PhysicalInventoryLineItemAdjustment for StockCardLineItem.
   *
   * @param parent the parent
   * @param dto the dto
   * @return new instance never null
   */
  public static PhysicalInventoryLineItemAdjustment newInstance(StockCardLineItem parent,
      PhysicalInventoryLineItemAdjustmentDto dto) {
    return new PhysicalInventoryLineItemAdjustment(null, parent, null, dto.getReason(),
        dto.getQuantity());
  }

  /**
   * Create new instance of PhysicalInventoryLineItemAdjustment for PhysicalInventoryLineItem.
   *
   * @param parent the parent
   * @param dto the dto
   * @return new instance never null
   */
  public static PhysicalInventoryLineItemAdjustment newInstance(PhysicalInventoryLineItem parent,
      PhysicalInventoryLineItemAdjustmentDto dto) {
    return new PhysicalInventoryLineItemAdjustment(null, null, parent, dto.getReason(),
        dto.getQuantity());
  }

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
