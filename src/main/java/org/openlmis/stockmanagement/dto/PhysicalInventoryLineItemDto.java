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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.dto.physicalinventory.PhysicalInventoryLineItemAdjustmentDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalInventoryLineItemDto implements IdentifiableByOrderableLot, VvmApplicable {
  private UUID orderableId;
  private UUID lotId;
  private Integer stockOnHand;
  private Integer quantity;
  private List<PhysicalInventoryLineItemAdjustmentDto> stockAdjustments;
  private Map<String, String> extraData;

  /**
   * Convert to jpa model.
   *
   * @param inventory inventory jpa model.
   * @return the converted jpa model.
   */
  public PhysicalInventoryLineItem toPhysicalInventoryLineItem(PhysicalInventory inventory) {
    final PhysicalInventoryLineItem item = PhysicalInventoryLineItem.builder()
        .orderableId(getOrderableId())
        .lotId(getLotId())
        .quantity(quantity)
        .extraData(extraData)
        .physicalInventory(inventory).build();

    item.setStockAdjustments(stockAdjustments != null ? stockAdjustments.stream().map(
        stockAdjustment -> PhysicalInventoryLineItemAdjustment.newInstance(item, stockAdjustment))
        .collect(toList()) : null);

    return item;
  }

  /**
   * Create from jpa model.
   *
   * @param lineItem line item jpa model.
   * @return created dto.
   */
  public static PhysicalInventoryLineItemDto from(PhysicalInventoryLineItem lineItem) {
    return PhysicalInventoryLineItemDto.builder().quantity(lineItem.getQuantity()).stockAdjustments(
        lineItem.getStockAdjustments() != null
            ? lineItem.getStockAdjustments().stream()
                .map(PhysicalInventoryLineItemAdjustmentDto::new)
                .collect(toList()) : null).extraData(lineItem.getExtraData())
        .orderableId(lineItem.getOrderableId()).lotId(lineItem.getLotId()).build();
  }

  /**
   * Create from Stock Event line item.
   *
   * @param lineItem stock event line item, not null
   * @return new Dto, never null
   */
  public static PhysicalInventoryLineItemDto from(StockEventLineItemDto lineItem) {
    return PhysicalInventoryLineItemDto.builder().quantity(lineItem.getQuantity()).stockAdjustments(
        lineItem.stockAdjustments().stream().map(PhysicalInventoryLineItemAdjustmentDto::new)
            .collect(toList())).extraData(lineItem.getExtraData())
        .orderableId(lineItem.getOrderableId()).lotId(lineItem.getLotId()).build();
  }

  /**
   * Create from event dtos.
   *
   * @param lineItems stock event line items.
   * @return created dtos.
   */
  public static List<PhysicalInventoryLineItemDto> from(List<StockEventLineItemDto> lineItems) {
    return lineItems
        .stream()
        .map(PhysicalInventoryLineItemDto::from)
        .collect(toList());
  }
}
