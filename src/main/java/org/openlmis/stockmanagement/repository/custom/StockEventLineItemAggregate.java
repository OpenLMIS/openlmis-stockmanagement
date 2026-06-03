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

package org.openlmis.stockmanagement.repository.custom;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

/**
 * Typed carrier for the per-event line-item aggregate used by the transaction history list.
 */
@Getter
public class StockEventLineItemAggregate {

  private final UUID stockEventId;
  private final int numberOfProducts;
  private final LocalDate occurredDate;

  /**
   * Creates an aggregate row; {@code numberOfProducts} comes in as a {@link Long} (from
   * {@code COUNT(DISTINCT ...)}) and is narrowed to an int.
   */
  public StockEventLineItemAggregate(UUID stockEventId, Long numberOfProducts,
      LocalDate occurredDate) {
    this.stockEventId = stockEventId;
    this.numberOfProducts = numberOfProducts == null ? 0 : numberOfProducts.intValue();
    this.occurredDate = occurredDate;
  }
}
