/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.stockmanagement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.hibernate.jpa.TypedParameterValue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCardLineItemRepository
        extends PagingAndSortingRepository<StockCardLineItem, UUID> {
    @Query(value = "SELECT EXISTS (SELECT 1 " +
            "FROM stockmanagement.stock_card_line_items scli " +
            "JOIN stockmanagement.stock_cards sc ON sc.id = scli.stockcardid " +
            "WHERE sc.facilityid= :facilityId " +
            "AND (cast(:lotId as uuid) IS NULL OR sc.lotId= :lotId) " +
            "AND sc.orderableid= :orderableId " +
            "AND (cast(:destinationId as uuid) IS NULL OR scli.destinationid= :destinationId) " +
            "AND (cast(:sourceId as uuid) IS NULL OR scli.sourceId= :sourceId) " +
            "AND scli.occurreddate= CAST(:occurredDate AS DATE) " +
            "AND scli.quantity = :quantity " +
            "AND (cast(:reasonId as uuid) IS NULL OR scli.reasonid= :reasonId) " +
            "AND (cast(:vvmStatus as VARCHAR) IS NULL OR scli.extradata ->> 'vvmStatus' = :vvmStatus))",
            nativeQuery = true)
    boolean getByAllGivenFields(
            UUID facilityId,
            TypedParameterValue lotId,
            UUID orderableId,
            TypedParameterValue destinationId,
            TypedParameterValue sourceId,
            String occurredDate,
            int quantity,
            TypedParameterValue reasonId,
            TypedParameterValue vvmStatus
    );

}
