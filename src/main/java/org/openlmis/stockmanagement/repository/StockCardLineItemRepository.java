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

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface StockCardLineItemRepository
    extends PagingAndSortingRepository<StockCardLineItem, UUID> {

  /**
   * Returns the ids of the stock cards touched by the given origin event. Uses a scalar
   * projection on purpose: hydrating the line-item entities would trigger {@code StockCard}'s
   * {@code @PostLoad} reorder mid-load, which can NPE on a not-yet-set occurredDate.
   */
  @Query("SELECT DISTINCT lineItem.stockCard.id FROM StockCardLineItem lineItem"
      + " WHERE lineItem.originEvent.id = :eventId")
  List<UUID> findStockCardIdsByOriginEvent(@Param("eventId") UUID eventId);
}
