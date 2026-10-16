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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.repository.custom.SubmittedPhysicalInventoryLine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PhysicalInventoriesRepository
    extends PagingAndSortingRepository<PhysicalInventory, UUID> {

  String PROGRAM_ID = "programId";
  String FACILITY_ID = "facilityId";

  List<PhysicalInventory> findByProgramIdAndFacilityIdAndIsDraft(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId,
      @Param("isDraft") boolean isDraft);

  List<PhysicalInventory> findByProgramIdAndFacilityId(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId);

  /**
   * Finds the counted products of the submitted physical inventories of the given facility and
   * program that occurred on or after the given date, limited to the given orderables. Used to
   * block cancellation of movements whose product and lot had a physical inventory recorded after
   * them: one lookup serves every line item of a stock event, and the caller matches the lot and
   * orders each inventory against its movement. No lot is bound as a parameter, so an untyped null
   * (which Postgres rejects) never reaches the query.
   *
   * <p>Two queries on purpose. Joining the inventory line items to the inventories in a single
   * statement lets Postgres scan every inventory line item of the instance, while starting from
   * the few inventories of one facility and program reaches their line items through the
   * physical inventory id index.
   *
   * @param programId    program id.
   * @param facilityId   facility id.
   * @param orderableIds ids of the orderables of the movements.
   * @param occurredDate the earliest occurred date of the movements.
   * @return one row per inventory line item, ordered by the inventory's occurred date.
   */
  default List<SubmittedPhysicalInventoryLine> findSubmittedLinesOfOrderablesFrom(
      UUID programId, UUID facilityId, Collection<UUID> orderableIds, LocalDate occurredDate) {
    if (orderableIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<UUID> inventoryIds = findSubmittedIdsFrom(programId, facilityId, occurredDate);
    return inventoryIds.isEmpty()
        ? Collections.emptyList()
        : findLinesOfOrderables(inventoryIds, orderableIds);
  }

  @Query("SELECT pi.id FROM PhysicalInventory pi"
      + " WHERE pi.programId = :programId"
      + " AND pi.facilityId = :facilityId"
      + " AND pi.isDraft = false"
      + " AND pi.occurredDate >= :occurredDate")
  List<UUID> findSubmittedIdsFrom(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId,
      @Param("occurredDate") LocalDate occurredDate);

  @Query("SELECT new org.openlmis.stockmanagement.repository.custom.SubmittedPhysicalInventoryLine("
      + "pi.id, pi.occurredDate, pi.documentNumber, se.processedDate, li.orderableId, li.lotId)"
      + " FROM PhysicalInventoryLineItem li"
      + " JOIN li.physicalInventory pi"
      + " LEFT JOIN pi.stockEvent se"
      + " WHERE li.physicalInventory.id IN :physicalInventoryIds"
      + " AND li.orderableId IN :orderableIds"
      + " ORDER BY pi.occurredDate")
  List<SubmittedPhysicalInventoryLine> findLinesOfOrderables(
      @Param("physicalInventoryIds") Collection<UUID> physicalInventoryIds,
      @Param("orderableIds") Collection<UUID> orderableIds);
}
