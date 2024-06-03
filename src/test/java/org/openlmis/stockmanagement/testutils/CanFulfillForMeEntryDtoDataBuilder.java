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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.web.stockcardsummariesv2.CanFulfillForMeEntryDto;

public class CanFulfillForMeEntryDtoDataBuilder {

  private ObjectReferenceDto stockCard;
  private ObjectReferenceDto orderable;
  private ObjectReferenceDto lot;
  private ObjectReferenceDto unitOfOrderable;
  private Integer stockOnHand;
  private LocalDate occurredDate;
  private ZonedDateTime processedDate;
  private boolean active;

  /**
   * Creates builder for creating new instance of {@link CanFulfillForMeEntryDto}.
   */
  public CanFulfillForMeEntryDtoDataBuilder() {
    stockCard = new ObjectReferenceDtoDataBuilder().withPath("api/stockCards").build();
    orderable = new ObjectReferenceDtoDataBuilder().withPath("api/orderables").build();
    lot = new ObjectReferenceDtoDataBuilder().withPath("api/lots").build();
    unitOfOrderable = new ObjectReferenceDtoDataBuilder().withPath("api/unitOfOrderables").build();
    stockOnHand = 10;
    occurredDate = LocalDate.now();
    active = true;
  }

  /**
   * Creates new instance of {@link CanFulfillForMeEntryDto} with properties.
   *
   * @return created can fulfill for me entry
   */
  public CanFulfillForMeEntryDto build() {
    return new CanFulfillForMeEntryDto(stockCard, orderable, lot, unitOfOrderable, stockOnHand,
        occurredDate, processedDate, active);
  }

  /**
   * Creates new instance of {@link CanFulfillForMeEntryDto} based on stock card and orderable.
   *
   * @return created can fulfill for me entry
   */
  public CanFulfillForMeEntryDto buildWithStockCardAndOrderable(StockCard stockCard,
                                                                OrderableDto orderable) {
    this
        .withOrderable(new ObjectReferenceDtoDataBuilder()
            .withPath("orderables")
            .withId(orderable.getId())
            .build());
    if (stockCard != null) {
      this
          .withStockOnHand(stockCard.getStockOnHand())
          .withStockCard(new ObjectReferenceDtoDataBuilder()
              .withPath("stockCards")
              .withId(stockCard.getId())
              .build())
          .withLot(new ObjectReferenceDtoDataBuilder()
              .withPath("lots")
              .withId(stockCard.getLotId())
              .build())
          .withUnitOfOrderable(new ObjectReferenceDtoDataBuilder()
              .withPath("unitOfOrderables")
              .withId(stockCard.getUnitOfOrderableId())
              .build())
          .withActive(stockCard.isActive())
          .withOccurredDate(stockCard.getOccurredDate())
          .withProcessedDate(stockCard.getProcessedDate());
    } else {
      this
          .withStockOnHand(0);
    }
    return build();
  }

  public CanFulfillForMeEntryDtoDataBuilder withStockOnHand(Integer stockOnHand) {
    this.stockOnHand = stockOnHand;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withStockCard(ObjectReferenceDto stockCard) {
    this.stockCard = stockCard;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withOrderable(ObjectReferenceDto orderable) {
    this.orderable = orderable;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withLot(ObjectReferenceDto lot) {
    this.lot = lot;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withUnitOfOrderable(
      ObjectReferenceDto unitOfOrderable) {
    this.unitOfOrderable = unitOfOrderable;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withOccurredDate(LocalDate occurredDate) {
    this.occurredDate = occurredDate;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withProcessedDate(ZonedDateTime processedDate) {
    this.processedDate = processedDate;
    return this;
  }

  public CanFulfillForMeEntryDtoDataBuilder withActive(boolean active) {
    this.active = active;
    return this;
  }

}
