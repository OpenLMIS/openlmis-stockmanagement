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

import java.util.UUID;
import org.openlmis.stockmanagement.domain.event.DocumentNumberSequence;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentNumberSequenceRepository
    extends PagingAndSortingRepository<DocumentNumberSequence, UUID> {

  @Transactional
  @Query(value = "INSERT INTO stockmanagement.document_number_sequences "
      + "(id, facilityid, year, month, lastsequencenumber) "
      + "VALUES (uuid_generate_v4(), :facilityId, :year, :month, 1) "
      + "ON CONFLICT ON CONSTRAINT document_number_sequences_facility_year_month_unique "
      + "DO UPDATE SET lastsequencenumber = "
      + "    stockmanagement.document_number_sequences.lastsequencenumber + 1 "
      + "RETURNING lastsequencenumber",
      nativeQuery = true)
  int nextSequenceNumber(@Param("facilityId") UUID facilityId,
                         @Param("year") int year,
                         @Param("month") int month);
}
