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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;

public class StockEventLineItemDtoDataBuilder {

  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private Integer quantity = 10;
  private Map<String, String> extraData = new HashMap<>();
  private LocalDate occurredDate = LocalDate.now();
  private UUID reasonId = UUID.randomUUID();
  private String reasonFreeText = RandomStringUtils.random(5);
  private UUID sourceId = UUID.randomUUID();
  private String sourceFreeText = RandomStringUtils.random(5);
  private UUID destinationId = UUID.randomUUID();
  private String destinationFreeText = RandomStringUtils.random(5);
  private List<StockEventAdjustmentDto> stockAdjustments = new ArrayList<>();
  private UUID unitOfOrderableId = UUID.randomUUID();

  /**
   * Builds Physical Inventory Event.
   */
  public StockEventLineItemDto buildForPhysicalInventory() {
    reasonId = null;
    reasonFreeText = null;
    noSourceAndDestination();
    return build();
  }

  /**
   * Builds Adjustment Event.
   */
  public StockEventLineItemDto buildForAdjustment() {
    noSourceAndDestination();
    return new StockEventLineItemDto(orderableId, lotId,quantity, extraData, occurredDate, reasonId,
        reasonFreeText, sourceId, sourceFreeText, destinationId, destinationFreeText,
        stockAdjustments, unitOfOrderableId);
  }

  /**
   * Build new Stock Event.
   */
  public StockEventLineItemDto build() {
    return new StockEventLineItemDto(orderableId, lotId,quantity, extraData, occurredDate, reasonId,
        reasonFreeText, sourceId, sourceFreeText, destinationId, destinationFreeText,
        stockAdjustments, unitOfOrderableId);
  }

  /**
   * Adds all stock adjustments, null will be ignored.
   */
  public StockEventLineItemDtoDataBuilder addStockAdjustments(
      Collection<StockEventAdjustmentDto> adjustments) {
    if (adjustments != null) {
      this.stockAdjustments.addAll(adjustments);
    }
    return this;
  }

  public StockEventLineItemDtoDataBuilder withQuantity(int quantity) {
    this.quantity = quantity;
    return this;
  }

  public StockEventLineItemDtoDataBuilder withOccurredDate(LocalDate date) {
    this.occurredDate = date;
    return this;
  }

  public StockEventLineItemDtoDataBuilder withReasonId(UUID reasonId) {
    this.reasonId = reasonId;
    return this;
  }

  public StockEventLineItemDtoDataBuilder withOrderableId(UUID orderableId) {
    this.orderableId = orderableId;
    return this;
  }

  private void noSourceAndDestination() {
    sourceId = null;
    sourceFreeText = null;
    destinationId = null;
    destinationFreeText = null;
  }
}
