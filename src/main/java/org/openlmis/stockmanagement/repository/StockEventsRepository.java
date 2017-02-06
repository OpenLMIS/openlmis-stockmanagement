package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface StockEventsRepository extends
        PagingAndSortingRepository<StockEvent, UUID> {
}
