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

package org.openlmis.stockmanagement.testutils;

import org.assertj.core.util.Lists;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;

import java.util.List;
import java.util.UUID;

public class StockCardDataBuilder {
  private UUID id = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private List<StockCardLineItem> lineItems = Lists.newArrayList();
  private Integer stockOnHand = 0;
  private StockEvent originalEvent;

  public StockCardDataBuilder(StockEvent originalEvent) {
    this.originalEvent = originalEvent;
  }

  /**
   * Sets id field as null. The event field will also have null value in id field.
   */
  public StockCardDataBuilder withoutId() {
    id = null;
    return this;
  }

  public StockCardDataBuilder withOrderable(UUID orderable) {
    orderableId = orderable;
    return this;
  }

  public StockCardDataBuilder withLot(UUID lot) {
    lotId = lot;
    return this;
  }

  public StockCardDataBuilder withLineItem(StockCardLineItem lineItem) {
    lineItems.add(lineItem);
    return this;
  }

  public StockCardDataBuilder withStockOnHand(Integer stockOnHand) {
    this.stockOnHand = stockOnHand;
    return this;
  }

  /**
   * Creates stock card based on parameters from the builder.
   */
  public StockCard buildWithStockOnHandAndLineItemAndOrderableId(Integer stockOnHand,
                                                                 StockCardLineItem lineItem,
                                                                 UUID orderableId) {
    return this
        .withOrderable(orderableId)
        .withStockOnHand(stockOnHand)
        .withLineItem(lineItem)
        .build();
  }

  /**
   * Creates stock card based on parameters from the builder.
   */
  public StockCard build() {
    StockCard card = new StockCard(
        originalEvent, originalEvent.getFacilityId(), originalEvent.getProgramId(), orderableId,
        lotId, lineItems, stockOnHand
    );
    card.setId(id);

    return card;
  }
}
