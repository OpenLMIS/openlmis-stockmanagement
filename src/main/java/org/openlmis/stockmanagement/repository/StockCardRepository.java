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
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("PMD.TooManyMethods")
public interface StockCardRepository extends JpaRepository<StockCard, UUID> {

  String PROGRAM_ID = "programId";
  String FACILITY_ID = "facilityId";

  Page<StockCard> findByProgramIdAndFacilityId(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId,
      Pageable pageable);

  List<StockCard> findByProgramIdAndFacilityId(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId);

  List<StockCard> findByFacilityId(UUID facilityId);

  List<StockCard> findByFacilityIdAndOrderableIdIn(UUID facilityId, Collection<UUID> orderableIds);

  List<StockCard> findByFacilityIdAndProgramIdIn(UUID facilityId, Collection<UUID> programIds);

  List<StockCard> findByFacilityIdAndOrderableIdInAndProgramIdIn(UUID facilityId,
      Collection<UUID> orderableIds, Collection<UUID> programIds);

  StockCard findByOriginEvent(@Param("originEventId") StockEvent stockEvent);

  @Query(name = StockCard.QUERY_FIND_LOT_IDENT_BY_PROG_FACILITY)
  List<OrderableLotIdentity> getIdentitiesBy(@Param(StockCard.PARAM_PROGRAM_ID) UUID programId,
                                             @Param(StockCard.PARAM_FACILITY_ID) UUID facilityId);

  Page<StockCard> findByFacilityIdInAndProgramIdInAndIdIn(Collection<UUID> facilityIds,
      Collection<UUID> programIds, Collection<UUID> ids, Pageable pageable);

  Page<StockCard> findByFacilityIdInAndProgramIdIn(Collection<UUID> facilityIds,
      Collection<UUID> programIds, Pageable pageable);

  Page<StockCard> findByIdIn(Collection<UUID> ids, Pageable pageable);

  List<StockCard> findByLotIdIn(Collection<UUID> lotIds);
}
