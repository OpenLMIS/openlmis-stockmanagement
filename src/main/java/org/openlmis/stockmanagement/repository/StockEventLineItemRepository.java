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

package org.openlmis.stockmanagement.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface StockEventLineItemRepository
    extends PagingAndSortingRepository<StockEventLineItem, UUID> {

  /**
   * Returns the cancellation line items that reverse any of the given original line items. Used to
   * detect which line items have already been cancelled.
   *
   * @param reversedLineItemIds ids of the original line items being checked.
   * @return the cancellation line items pointing at any of the given ids.
   */
  List<StockEventLineItem> findByReversesEventLineItemIdIn(Collection<UUID> reversedLineItemIds);

  /**
   * Returns the cancellation line items (those that reverse another line) belonging to any of the
   * given stock events. Used to detect which shown events are cancellations and which original
   * line they reverse.
   *
   * @param stockEventIds ids of the stock events shown on the page.
   * @return the cancellation line items of those events.
   */
  List<StockEventLineItem> findByStockEventIdInAndReversesEventLineItemIdIsNotNull(
      Collection<UUID> stockEventIds);

  /**
   * Returns the line items of the given stock events for the given orderables. Used to resolve the
   * original line item behind each shown line (matched by event + orderable + lot).
   *
   * @param stockEventIds ids of the stock events shown on the page.
   * @param orderableIds  ids of the orderables shown on the page.
   * @return the matching line items.
   */
  List<StockEventLineItem> findByStockEventIdInAndOrderableIdIn(
      Collection<UUID> stockEventIds, Collection<UUID> orderableIds);
}
