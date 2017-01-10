package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface AvailableStockCardFieldsRepository extends
        PagingAndSortingRepository<AvailableStockCardFields, UUID> {
}
