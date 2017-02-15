package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.adjustment.ReasonCategory;
import org.openlmis.stockmanagement.domain.adjustment.ReasonType;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockCardLineItemReasonRepository extends
        PagingAndSortingRepository<StockCardLineItemReason, UUID> {
  StockCardLineItemReason findByReasonTypeAndReasonCategory(
          @Param("reasonType") ReasonType reasonType,
          @Param("reasonCategory") ReasonCategory reasonCategory);
}
