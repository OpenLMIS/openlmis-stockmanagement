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

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockCardRepository extends
    PagingAndSortingRepository<StockCard, UUID> {

  String selectId = "select s.id ";
  String selectOrderableId = "select s.orderableId ";
  String selectLotId = "select s.lotId ";

  String fromStockCards = "from org.openlmis.stockmanagement.domain.card.StockCard s ";

  String matchByProgramAndFacility = "where s.programId = ?1 and s.facilityId = ?2 ";
  String matchByOrderable = "and s.orderableId = ?3 ";
  String matchByLot = "and s.lotId = ?4";
  String noLot = "and s.lotId IS NULL";

  StockCard findByProgramIdAndFacilityIdAndOrderableIdAndLotId(
      @Param("programId") UUID programId,
      @Param("facilityId") UUID facilityId,
      @Param("orderableId") UUID orderableId,
      @Param("lotId") UUID lotId);

  Page<StockCard> findByProgramIdAndFacilityId(
      @Param("programId") UUID programId,
      @Param("facilityId") UUID facilityId,
      Pageable pageable);

  StockCard findByOriginEvent(@Param("originEventId") StockEvent stockEvent);

  //the following is for performance optimization
  //when saving a new stock card line item, only need to find existing card id
  //fetching the whole card including line items takes too long
  @Query(value = selectId + fromStockCards
      + matchByProgramAndFacility + matchByOrderable + matchByLot)
  UUID getStockCardIdByWithNoLot(UUID programId, UUID facilityId, UUID orderableId, UUID lotId);

  @Query(value = selectId + fromStockCards
      + matchByProgramAndFacility + matchByOrderable + noLot)
  UUID getStockCardIdByWithNoLot(UUID programId, UUID facilityId, UUID orderableId);

  @Query(value = selectOrderableId + fromStockCards + matchByProgramAndFacility)
  List<UUID> getStockCardOrderableIdsBy(UUID programId, UUID facilityId);

  @Query(value = selectLotId + fromStockCards + matchByProgramAndFacility)
  List<UUID> getStockCardLotIdsBy(UUID programId, UUID facilityId);
}
