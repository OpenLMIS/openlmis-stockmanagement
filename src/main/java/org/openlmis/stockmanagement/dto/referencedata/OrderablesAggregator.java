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

package org.openlmis.stockmanagement.dto.referencedata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.data.domain.Page;

public class OrderablesAggregator {

  private final List<ApprovedProductDto> approvedProducts;
  private List<OrderableDto> orderables = new ArrayList<>();
  private List<UUID> identifiers = new ArrayList<>();

  public OrderablesAggregator(List<ApprovedProductDto> approvedProducts) {
    this.approvedProducts = approvedProducts;
    this.approvedProducts.forEach(this::addEntry);
  }

  public Page<ApprovedProductDto> getApprovedProducts() {
    return Pagination.getPage(approvedProducts);
  }

  public Page<OrderableDto> getOrderablesPage() {
    return Pagination.getPage(orderables);
  }

  public List<UUID> getIdentifiers() {
    return identifiers;
  }

  private void addEntry(ApprovedProductDto entry) {
    orderables.add(entry.getOrderable());
    identifiers.add(entry.getOrderable().getId());
  }
}
