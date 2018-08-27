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

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;

public class PhysicalInventoryLineItemDtoTest {
  @Test
  public void shouldCreateDtoFromJpaModel() throws Exception {
    //given
    PhysicalInventoryLineItem lineItem = PhysicalInventoryLineItem.builder()
        .quantity(123)
        .orderableId(UUID.randomUUID())
        .lotId(UUID.randomUUID())
        .stockAdjustments(singletonList(createStockAdjustment()))
        .build();

    //when
    PhysicalInventoryLineItemDto lineItemDto = PhysicalInventoryLineItemDto.from(lineItem);

    //then
    assertThat(lineItemDto.getQuantity(), is(lineItem.getQuantity()));
    assertThat(lineItemDto.getOrderableId(), is(lineItem.getOrderableId()));
    assertThat(lineItemDto.getLotId(), is(lineItem.getLotId()));
    assertThat(lineItemDto.getStockAdjustments(), is(lineItem.getStockAdjustments()));
  }

  private PhysicalInventoryLineItemAdjustment createStockAdjustment() {
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name("test reason")
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.TRANSFER)
        .isFreeTextAllowed(false)
        .build();

    return PhysicalInventoryLineItemAdjustment.builder()
        .quantity(10)
        .reason(reason)
        .build();
  }
}