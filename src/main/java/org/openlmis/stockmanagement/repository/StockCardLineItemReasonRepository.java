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

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface StockCardLineItemReasonRepository extends
    PagingAndSortingRepository<StockCardLineItemReason, UUID> {
  List<StockCardLineItemReason> findAll();

  StockCardLineItemReason findByName(@Param("name") String name);

  List<StockCardLineItemReason> findByIdIn(Collection<UUID> ids);

  List<StockCardLineItemReason> findByReasonTypeIn(Collection<ReasonType> types);

  @Query("SELECT DISTINCT t FROM StockCardLineItemReason r JOIN r.tags AS t")
  List<String> findTags();

  @Query("SELECT CASE WHEN COUNT(1) > 0 THEN true ELSE false END"
      + " FROM StockCardLineItemReason r"
      + " JOIN r.tags AS t"
      + " WHERE t = :tag")
  Boolean existsByTag(@Param("tag") String tag);
}
