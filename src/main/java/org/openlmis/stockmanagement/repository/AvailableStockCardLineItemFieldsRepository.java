package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface AvailableStockCardLineItemFieldsRepository extends
        PagingAndSortingRepository<AvailableStockCardLineItemFields, UUID> {
}
