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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.physicalinventory.PhysicalInventoryLineItemAdjustmentDto;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalInventoryLineItemAdjustmentTest {
  @Mock
  private StockEventLineItem testStockEventLineItem;

  @Mock
  private StockCardLineItem testStockCardLineItem;

  @Mock
  private PhysicalInventoryLineItem testPhysicalInventoryLineItem;

  @Mock
  private StockCardLineItemReason testReason;
  private Integer testQuantity = 123;

  @Test
  public void shouldCreateFromStockEventLineItem() {
    final PhysicalInventoryLineItemAdjustmentDto dto = createDto();
    final PhysicalInventoryLineItemAdjustment adjustment =
        PhysicalInventoryLineItemAdjustment.newInstance(testStockEventLineItem, dto);

    assertEquals(adjustment.getStockEventLineItem(), testStockEventLineItem);
    assertNull(adjustment.getStockCardLineItem());
    assertNull(adjustment.getPhysicalInventoryLineItem());
    assertEquals(adjustment.getReason(), testReason);
    assertEquals(adjustment.getQuantity(), testQuantity);
  }

  @Test
  public void shouldCreateFromStockCardLineItem() {
    final PhysicalInventoryLineItemAdjustmentDto dto = createDto();
    final PhysicalInventoryLineItemAdjustment adjustment =
        PhysicalInventoryLineItemAdjustment.newInstance(testStockCardLineItem, dto);

    assertNull(adjustment.getStockEventLineItem());
    assertEquals(adjustment.getStockCardLineItem(), testStockCardLineItem);
    assertNull(adjustment.getPhysicalInventoryLineItem());
    assertEquals(adjustment.getReason(), testReason);
    assertEquals(adjustment.getQuantity(), testQuantity);
  }

  @Test
  public void shouldCreateFromPhysicalInventoryLineItem() {
    final PhysicalInventoryLineItemAdjustmentDto dto = createDto();
    final PhysicalInventoryLineItemAdjustment adjustment =
        PhysicalInventoryLineItemAdjustment.newInstance(testPhysicalInventoryLineItem, dto);

    assertNull(adjustment.getStockEventLineItem());
    assertNull(adjustment.getStockCardLineItem());
    assertEquals(adjustment.getPhysicalInventoryLineItem(), testPhysicalInventoryLineItem);
    assertEquals(adjustment.getReason(), testReason);
    assertEquals(adjustment.getQuantity(), testQuantity);
  }

  private PhysicalInventoryLineItemAdjustmentDto createDto() {
    return PhysicalInventoryLineItemAdjustmentDto.builder().reason(testReason)
        .quantity(testQuantity).build();
  }
}
