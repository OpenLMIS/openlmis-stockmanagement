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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PhysicalInventoriesRepository
    extends PagingAndSortingRepository<PhysicalInventory, UUID> {

  List<PhysicalInventory> findByProgramIdAndFacilityIdAndIsDraft(
      @Param("programId") UUID programId,
      @Param("facilityId") UUID facilityId,
      @Param("isDraft") boolean isDraft);

  List<PhysicalInventory> findByProgramIdAndFacilityId(
      @Param("programId") UUID programId,
      @Param("facilityId") UUID facilityId);

  /**
   * Finds submitted physical inventories for the given facility, program, orderable and lot that
   * occurred after the given date. Used to block cancellation of a movement whose product and lot
   * had a physical inventory recorded after it.
   *
   * @param programId    program id.
   * @param facilityId   facility id.
   * @param orderableId  orderable id.
   * @param lotId        lot id, may be null.
   * @param occurredDate the movement's occurred date; inventories strictly after it are returned.
   * @return the blocking submitted physical inventories.
   */
  @Query("SELECT DISTINCT pi FROM PhysicalInventory pi"
      + " JOIN pi.lineItems li"
      + " WHERE pi.programId = :programId"
      + " AND pi.facilityId = :facilityId"
      + " AND pi.isDraft = false"
      + " AND pi.occurredDate > :occurredDate"
      + " AND li.orderableId = :orderableId"
      + " AND ((:lotId IS NULL AND li.lotId IS NULL) OR li.lotId = :lotId)")
  List<PhysicalInventory> findSubmittedAfter(
      @Param("programId") UUID programId,
      @Param("facilityId") UUID facilityId,
      @Param("orderableId") UUID orderableId,
      @Param("lotId") UUID lotId,
      @Param("occurredDate") LocalDate occurredDate);
}
