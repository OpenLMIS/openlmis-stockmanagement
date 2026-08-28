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
  private final int entriesCount;
  private final int originalEntriesCount;
  private final LocalDate occurredDate;

  /**
   * Creates an aggregate row; the counts arrive as {@link Long} and are narrowed to int.
   * {@code originalEntriesCount} counts the line items that do not reverse another one.
   */
  public StockEventLineItemAggregate(UUID stockEventId, Long entriesCount,
      Long originalEntriesCount, LocalDate occurredDate) {
    this.stockEventId = stockEventId;
    this.entriesCount = entriesCount == null ? 0 : entriesCount.intValue();
    this.originalEntriesCount = originalEntriesCount == null ? 0 : originalEntriesCount.intValue();
    this.occurredDate = occurredDate;
  }
}
