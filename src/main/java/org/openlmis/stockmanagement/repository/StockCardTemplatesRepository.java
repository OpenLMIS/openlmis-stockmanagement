package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockCardTemplatesRepository extends
        PagingAndSortingRepository<StockCardTemplate, UUID> {

  StockCardTemplate findByProgramIdAndFacilityTypeId(
          @Param("programId") UUID programId, @Param("facilityTypeId") UUID facilityTypeId);
}
