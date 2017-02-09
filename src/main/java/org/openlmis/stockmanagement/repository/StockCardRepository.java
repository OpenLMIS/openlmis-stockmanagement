package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockCardRepository extends
        PagingAndSortingRepository<StockCard, UUID> {

  StockCard findByProgramIdAndFacilityIdAndOrderableId(
          @Param("programId") UUID programId,
          @Param("facilityId") UUID facilityId,
          @Param("orderableId") UUID orderableId);

  StockCard findByOriginEvent(@Param("originEventId") StockEvent stockEvent);
}
