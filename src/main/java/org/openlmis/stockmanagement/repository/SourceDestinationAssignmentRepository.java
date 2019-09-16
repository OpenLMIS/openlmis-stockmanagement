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

import org.openlmis.stockmanagement.domain.sourcedestination.SourceDestinationAssignment;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface SourceDestinationAssignmentRepository<T extends SourceDestinationAssignment>
    extends PagingAndSortingRepository<T, UUID> {

  List<T> findAll();

  List<T> findByProgramIdAndFacilityTypeId(
      @Param("programId") UUID programId, @Param("facilityTypeId") UUID facilityTypeId);

  T findByProgramIdAndFacilityTypeIdAndNodeId(
      @Param("programId") UUID programId, @Param("facilityTypeId") UUID facilityTypeId,
      @Param("nodeId") UUID nodeId);
}
