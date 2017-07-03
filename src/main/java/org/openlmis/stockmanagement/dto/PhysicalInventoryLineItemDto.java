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

package org.openlmis.stockmanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalInventoryLineItemDto implements IdentifiableByOrderableLot {
  private OrderableDto orderable;
  private LotDto lot;
  private Integer stockOnHand;
  private Integer quantity;
  private List<StockAdjustment> stockAdjustments;
  private Map<String, String> extraData;

  /**
   * Convert to jpa model.
   *
   * @param inventory inventory jpa model.
   * @return the converted jpa model.
   */
  public PhysicalInventoryLineItem toPhysicalInventoryLineItem(PhysicalInventory inventory) {
    return PhysicalInventoryLineItem.builder()
        .orderableId(getOrderableId())
        .lotId(getLotId())
        .quantity(quantity)
        .stockAdjustments(stockAdjustments)
        .extraData(extraData)
        .physicalInventory(inventory).build();
  }

  /**
   * Create from jpa model.
   *
   * @param lineItem line item jpa model.
   * @return created dto.
   */
  public static PhysicalInventoryLineItemDto from(PhysicalInventoryLineItem lineItem) {
    return PhysicalInventoryLineItemDto
        .builder()
        .quantity(lineItem.getQuantity())
        .stockAdjustments(lineItem.getStockAdjustments())
        .extraData(lineItem.getExtraData())
        .orderable(OrderableDto.builder().id(lineItem.getOrderableId()).build())
        .lot(lineItem.getLotId() == null ? null : LotDto.builder().id(lineItem.getLotId()).build())
        .build();
  }

  @JsonIgnore
  public UUID getOrderableId() {
    return orderable.getId();
  }

  @JsonIgnore
  public UUID getLotId() {
    return lot == null ? null : lot.getId();
  }
}
