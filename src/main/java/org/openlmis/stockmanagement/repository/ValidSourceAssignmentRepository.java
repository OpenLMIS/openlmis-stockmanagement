package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ValidSourceAssignmentRepository extends
        PagingAndSortingRepository<ValidSourceAssignment, UUID> {

  List<ValidSourceAssignment> findByProgramIdAndFacilityTypeId(
          @Param("programId") UUID programId, @Param("facilityTypeId") UUID facilityTypeId);
}
