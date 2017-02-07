package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface StockCardLineItemsRepository extends
        PagingAndSortingRepository<StockCardLineItem, UUID> {
}
