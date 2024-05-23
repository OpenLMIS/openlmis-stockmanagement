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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDate;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;

@SuppressWarnings("PMD.TooManyMethods")
public class StockCardLineItemDataBuilder {
  private UUID id = UUID.randomUUID();
  private StockCard stockCard = null;
  private StockEvent originEvent = null;
  private Integer quantity = 0;
  private Map<String, String> extraData = ImmutableMap.of("extra-key", "value");
  private StockCardLineItemReason reason = null;
  private String sourceFreeText = EMPTY;
  private String destinationFreeText = EMPTY;
  private String documentNumber = EMPTY;
  private String reasonFreeText = EMPTY;
  private String signature = EMPTY;
  private Node source = null;
  private Node destination = null;
  private LocalDate occurredDate = getBaseDate();
  private ZonedDateTime processedDateTime = getBaseDateTime();
  private UUID userId = UUID.randomUUID();
  private UUID unitOfOrderableId = UUID.randomUUID();
  private Integer stockOnHand = 0;
  private List<PhysicalInventoryLineItemAdjustment> stockAdjustments = Lists.newArrayList();

  public StockCardLineItemDataBuilder withCreditReason() {
    return this.withReason(new StockCardLineItemReasonDataBuilder().withCreditType().build());
  }

  public StockCardLineItemDataBuilder withDebitReason() {
    return this.withReason(new StockCardLineItemReasonDataBuilder().withDebitType().build());
  }

  public StockCardLineItemDataBuilder withReason(StockCardLineItemReason reason) {
    this.reason = reason;
    return this;
  }

  public StockCardLineItemDataBuilder withQuantity(int newQuantity) {
    quantity = newQuantity;
    return this;
  }

  public StockCardLineItemDataBuilder withStockOnHand(int newStockOnHand) {
    stockOnHand = newStockOnHand;
    return this;
  }

  public StockCardLineItemDataBuilder withOccurredDatePreviousDay() {
    return this.withOccurredDate(occurredDate.minusDays(1));
  }

  public StockCardLineItemDataBuilder withOccurredDateNextDay() {
    return this.withOccurredDate(occurredDate.plusDays(1));
  }

  public StockCardLineItemDataBuilder withOccurredDate(LocalDate occurredDate) {
    this.occurredDate = occurredDate;
    return this;
  }

  public StockCardLineItemDataBuilder withProcessedDateNextDay() {
    processedDateTime = processedDateTime.plusDays(1);
    return this;
  }

  public StockCardLineItemDataBuilder withProcessedDateHourEarlier() {
    processedDateTime = processedDateTime.minusHours(1);
    return this;
  }

  public StockCardLineItemDataBuilder withStockCard(StockCard card) {
    this.stockCard = card;
    return this;
  }

  public StockCardLineItemDataBuilder withoutId() {
    id = null;
    return this;
  }

  public StockCardLineItemDataBuilder withOriginEvent(StockEvent event) {
    originEvent = event;
    return this;
  }

  /**
   * Creates new instance of {@link StockCardLineItem} with properties.
   *
   * @param reasonType   reason type that will be assigned to stock card line item reason
   * @param tags         list of tags assigned to stock reason
   * @param quantity     quantity stored in stock card line item
   * @param occurredDate date that stock change occurred
   * @return created line item.
   */
  public StockCardLineItem buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
      ReasonType reasonType, List<String> tags, Integer quantity, LocalDate occurredDate) {
    return this
        .withReason(new StockCardLineItemReasonDataBuilder()
            .withReasonType(reasonType)
            .withTags(tags)
            .build())
        .withQuantity(quantity)
        .withOccurredDate(occurredDate)
        .build();
  }

  /**
   * Creates new instance of {@link StockCardLineItem} with properties.
   *
   * @return created line item.
   */
  public StockCardLineItem buildWithStockOnHand(int stockOnHand) {
    return this.withStockOnHand(stockOnHand).build();
  }

  /**
   * Creates new instance of {@link StockCardLineItem} with properties.
   *
   * @return created line item.
   */
  public StockCardLineItem build() {
    StockCardLineItem lineItem = new StockCardLineItem(
        stockCard, originEvent, quantity, extraData, reason, sourceFreeText, destinationFreeText,
        documentNumber, reasonFreeText, signature, source, destination, occurredDate,
        processedDateTime, userId, unitOfOrderableId, stockOnHand, stockAdjustments
    );
    lineItem.setId(id);

    return lineItem;
  }
}
