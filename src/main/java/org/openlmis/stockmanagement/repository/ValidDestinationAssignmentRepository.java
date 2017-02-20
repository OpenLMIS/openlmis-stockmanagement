package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ValidDestinationAssignmentRepository extends
        PagingAndSortingRepository<ValidDestinationAssignment, UUID> {

  List<ValidDestinationAssignment> findByProgramIdAndFacilityTypeId(
          @Param("programId") UUID programId, @Param("facilityTypeId") UUID facilityTypeId);
}
