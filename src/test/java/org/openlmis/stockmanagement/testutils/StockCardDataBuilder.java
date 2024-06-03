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

import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDate;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDateTime;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;

@SuppressWarnings("PMD.TooManyMethods")
public class StockCardDataBuilder {
  private UUID id = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private UUID unitOfOrderableId = UUID.randomUUID();
  private List<StockCardLineItem> lineItems = Lists.newArrayList();
  private Integer stockOnHand = 0;
  private LocalDate occurredDate = getBaseDate();
  private ZonedDateTime processedDate = getBaseDateTime();
  private StockEvent originalEvent;
  private boolean isActive = true;

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

  public StockCardDataBuilder withOrderableId(UUID orderableId) {
    this.orderableId = orderableId;
    return this;
  }

  public StockCardDataBuilder withLotId(UUID lotId) {
    this.lotId = lotId;
    return this;
  }

  public StockCardDataBuilder withUnitOfOrderableId(UUID unitOfOrderableId) {
    this.unitOfOrderableId = unitOfOrderableId;
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
  
  public StockCardDataBuilder withOccurredDate(LocalDate date) {
    this.occurredDate = date;
    return this;
  }
  
  public StockCardDataBuilder withProcessedDate(ZonedDateTime date) {
    this.processedDate = date;
    return this;
  }

  public StockCardDataBuilder withIsActive(boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * Creates stock card based on parameters from the builder.
   */
  public StockCard build() {
    StockCard card = new StockCard(
        originalEvent, originalEvent.getFacilityId(), originalEvent.getProgramId(), orderableId,
        lotId, unitOfOrderableId, lineItems, stockOnHand, occurredDate, processedDate, isActive
    );
    card.setId(id);

    return card;
  }
}
