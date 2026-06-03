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
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.repository.custom.StockEventLineItemAggregate;
import org.openlmis.stockmanagement.repository.custom.StockEventsRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface StockEventsRepository extends
    PagingAndSortingRepository<StockEvent, UUID>, StockEventsRepositoryCustom {

  /**
   * Aggregates the line items of the given events in one query, so building the history rows
   * does not trigger an N+1 over each event's line items.
   *
   * @param eventIds the stock event ids of the current page.
   * @return one aggregate (event id, distinct product count, earliest occurred date) per event
   *         that has line items.
   */
  @Query("SELECT new org.openlmis.stockmanagement.repository.custom.StockEventLineItemAggregate("
      + "lineItem.stockEvent.id, COUNT(DISTINCT lineItem.orderableId),"
      + " MIN(lineItem.occurredDate))"
      + " FROM StockEventLineItem lineItem"
      + " WHERE lineItem.stockEvent.id IN :eventIds"
      + " GROUP BY lineItem.stockEvent.id")
  List<StockEventLineItemAggregate> aggregateLineItemsByEventIds(
      @Param("eventIds") Collection<UUID> eventIds);
}
