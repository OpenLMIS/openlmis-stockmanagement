package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface StockCardRepository extends
        PagingAndSortingRepository<StockCard, UUID> {
}
