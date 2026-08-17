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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PhysicalInventoriesRepository
    extends PagingAndSortingRepository<PhysicalInventory, UUID> {

  String PROGRAM_ID = "programId";
  String FACILITY_ID = "facilityId";
  String ORDERABLE_ID = "orderableId";
  String OCCURRED_DATE = "occurredDate";
  String PROCESSED_DATE = "processedDate";

  List<PhysicalInventory> findByProgramIdAndFacilityIdAndIsDraft(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId,
      @Param("isDraft") boolean isDraft);

  List<PhysicalInventory> findByProgramIdAndFacilityId(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId);

  /**
   * Finds submitted physical inventories for the given facility, program, orderable and lot that
   * occurred after the given date. Used to block cancellation of a movement whose product and lot
   * had a physical inventory recorded after it. Kept separate from the no-lot variant so the
   * {@code lotId} parameter is never bound as an untyped null (which Postgres rejects).
   *
   * @param programId    program id.
   * @param facilityId   facility id.
   * @param orderableId  orderable id.
   * @param lotId        lot id, must not be null.
   * @param occurredDate the movement's occurred date.
   * @param processedDate the movement's processed date; used as the tiebreaker on equal occurred
   *                      dates, matching StockCard.getLineItemsComparator() ordering.
   * @return the blocking submitted physical inventories (ordered after the movement).
   */
  @Query("SELECT DISTINCT pi FROM PhysicalInventory pi"
      + " JOIN pi.lineItems li"
      + " LEFT JOIN pi.stockEvent se"
      + " WHERE pi.programId = :programId"
      + " AND pi.facilityId = :facilityId"
      + " AND pi.isDraft = false"
      + " AND (pi.occurredDate > :occurredDate"
      + "   OR (pi.occurredDate = :occurredDate AND se.processedDate > :processedDate))"
      + " AND li.orderableId = :orderableId"
      + " AND li.lotId = :lotId")
  List<PhysicalInventory> findSubmittedAfterForOrderableAndLot(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId,
      @Param(ORDERABLE_ID) UUID orderableId,
      @Param("lotId") UUID lotId,
      @Param(OCCURRED_DATE) LocalDate occurredDate,
      @Param(PROCESSED_DATE) ZonedDateTime processedDate);

  /**
   * Same as {@link #findSubmittedAfterForOrderableAndLot} but for line items without a lot.
   *
   * @param programId    program id.
   * @param facilityId   facility id.
   * @param orderableId  orderable id.
   * @param occurredDate the movement's occurred date.
   * @param processedDate the movement's processed date; used as the tiebreaker on equal occurred
   *                      dates, matching StockCard.getLineItemsComparator() ordering.
   * @return the blocking submitted physical inventories (ordered after the movement).
   */
  @Query("SELECT DISTINCT pi FROM PhysicalInventory pi"
      + " JOIN pi.lineItems li"
      + " LEFT JOIN pi.stockEvent se"
      + " WHERE pi.programId = :programId"
      + " AND pi.facilityId = :facilityId"
      + " AND pi.isDraft = false"
      + " AND (pi.occurredDate > :occurredDate"
      + "   OR (pi.occurredDate = :occurredDate AND se.processedDate > :processedDate))"
      + " AND li.orderableId = :orderableId"
      + " AND li.lotId IS NULL")
  List<PhysicalInventory> findSubmittedAfterForOrderableWithoutLot(
      @Param(PROGRAM_ID) UUID programId,
      @Param(FACILITY_ID) UUID facilityId,
      @Param(ORDERABLE_ID) UUID orderableId,
      @Param(OCCURRED_DATE) LocalDate occurredDate,
      @Param(PROCESSED_DATE) ZonedDateTime processedDate);
}
