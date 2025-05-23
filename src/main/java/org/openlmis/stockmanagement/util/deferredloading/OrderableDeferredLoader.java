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

package org.openlmis.stockmanagement.util.deferredloading;

import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

public class OrderableDeferredLoader
    extends DeferredLoader<OrderableDto, UUID, OrderableDeferredLoader.Handle> {
  private OrderableReferenceDataService orderableReferenceDataService;

  public OrderableDeferredLoader(OrderableReferenceDataService orderableReferenceDataService) {
    this.orderableReferenceDataService = orderableReferenceDataService;
  }

  @Override
  protected Handle newHandle(UUID key) {
    return new Handle(key);
  }

  @Override
  public void loadDeferredObjects() {
    final List<OrderableDto> allDeferredOrderables =
        orderableReferenceDataService.findByIds(deferredObjects.keySet());

    for (OrderableDto orderable : allDeferredOrderables) {
      deferredObjects.get(orderable.getId()).set(orderable);
    }

    deferredObjects.clear();
  }

  public static class Handle extends DeferredObject<OrderableDto, UUID> {
    public Handle(UUID objectKey) {
      super(objectKey);
    }
  }
}
