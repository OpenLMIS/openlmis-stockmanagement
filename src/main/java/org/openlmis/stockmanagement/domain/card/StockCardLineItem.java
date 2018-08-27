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

package org.openlmis.stockmanagement.domain.card;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static java.time.ZonedDateTime.now;
import static javax.persistence.CascadeType.ALL;
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.physicalBalance;
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.physicalCredit;
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.physicalDebit;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERRRO_EVENT_SOH_EXCEEDS_LIMIT;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.ExtraDataConverter;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Getter
@Setter
@Builder
@ToString(exclude = "stockCard")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({
    "stockCard", "originEvent",
    "source", "destination",
    "processedDate",
    "userId"})
@Table(name = "stock_card_line_items", schema = "stockmanagement")
public class StockCardLineItem extends BaseEntity {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardLineItem.class);

  @ManyToOne
  @JoinColumn(nullable = false)
  private StockCard stockCard;

  @ManyToOne
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  private Map<String, String> extraData;

  @ManyToOne
  @JoinColumn
  private StockCardLineItemReason reason;

  private String sourceFreeText;
  private String destinationFreeText;
  private String documentNumber;
  private String reasonFreeText;
  private String signature;

  @ManyToOne
  @JoinColumn
  private Node source;

  @ManyToOne
  @JoinColumn
  private Node destination;

  @Column(nullable = false)
  @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
  private LocalDate occurredDate;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime processedDate;

  @Column(nullable = false)
  private UUID userId;

  @Transient
  private Integer stockOnHand;

  @OneToMany(
      cascade = ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JoinColumn(name = "stockCardLineItemId")
  private List<PhysicalInventoryLineItemAdjustment> stockAdjustments;

  /**
   * Create line item from eventDto.
   *
   * @param eventDto     stock eventDto.
   * @param stockCard    the card that this line item belongs to.
   * @param savedEventId saved event id.
   * @return created line item.
   */
  public static StockCardLineItem createLineItemFrom(
      StockEventDto eventDto, StockEventLineItemDto eventLineItem,
      StockCard stockCard, UUID savedEventId) {
    StockCardLineItemBuilder builder = StockCardLineItem.builder();

    if (null != savedEventId) {
      StockEvent event = new StockEvent();
      event.setId(savedEventId);
      builder = builder.originEvent(event);
    }

    if (null != eventLineItem.getReasonId()) {
      StockCardLineItemReason reason = new StockCardLineItemReason();
      reason.setId(eventLineItem.getReasonId());
      builder = builder.reason(reason);
    }

    if (null != eventLineItem.getSourceId()) {
      Node source = new Node();
      source.setId(eventLineItem.getSourceId());
      builder = builder.source(source);
    }

    if (null != eventLineItem.getDestinationId()) {
      Node destination = new Node();
      destination.setId(eventLineItem.getDestinationId());
      builder = builder.destination(destination);
    }

    StockCardLineItem cardLineItem = builder
        .stockCard(stockCard)

        .quantity(eventLineItem.getQuantity())
        .stockAdjustments(eventLineItem.stockAdjustments())

        .occurredDate(eventLineItem.getOccurredDate())
        .processedDate(now())

        .reasonFreeText(eventLineItem.getReasonFreeText())
        .sourceFreeText(eventLineItem.getSourceFreeText())
        .destinationFreeText(eventLineItem.getDestinationFreeText())

        .documentNumber(eventDto.getDocumentNumber())
        .signature(eventDto.getSignature())
        .userId(eventDto.getContext().getCurrentUserId())

        .stockOnHand(0)

        .extraData(eventLineItem.getExtraData())
        .build();

    stockCard.getLineItems().add(cardLineItem);

    return cardLineItem;
  }

  /**
   * Calculate stock on hand with previous stock on hand.
   *
   * @param previousStockOnHand previous stock on hand.
   */
  public void calculateStockOnHand(int previousStockOnHand) {
    if (isPhysicalInventory()) {
      setReason(determineReasonByQuantity(previousStockOnHand));
      setStockOnHand(quantity);
      setQuantity(Math.abs(getStockOnHand() - previousStockOnHand));
      LOGGER.debug("Physical inventory: " + getStockOnHand());
    } else if (shouldIncrease()) {
      tryIncrease(previousStockOnHand);
    } else {
      tryDecrease(previousStockOnHand);
    }
  }

  /**
   * Returns quantity value with correct sign depending on reason type.
   *
   * @return quantity value, is negative for Debit reason
   */
  public Integer getQuantityWithSign() {
    if (null == this.getQuantity()) {
      return 0;
    }
    return this.shouldIncrease()
        ? this.getQuantity()
        : this.getQuantity() * -1;
  }

  /**
   * Checks if assigned reason has tag assigned.
   *
   * @param tag string with tag value
   * @return true if there is reason assigned and has given tag
   */
  public boolean containsTag(String tag) {
    return null != this.getReason() && this.getReason().getTags().contains(tag);
  }

  private void tryDecrease(int previousStockOnHand) {
    if (previousStockOnHand - quantity < 0) {
      throw new ValidationMessageException(
          new Message(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH, previousStockOnHand, quantity));
    }

    setStockOnHand(previousStockOnHand - quantity);
    LOGGER.debug(previousStockOnHand + " - " + quantity + " = " + getStockOnHand());
  }

  private void tryIncrease(int previousStockOnHand) {
    try {
      //this may exceed max of integer
      setStockOnHand(Math.addExact(previousStockOnHand, quantity));
      LOGGER.debug(previousStockOnHand + " + " + quantity + " = " + getStockOnHand());
    } catch (ArithmeticException ex) {
      throw new ValidationMessageException(
          new Message(ERRRO_EVENT_SOH_EXCEEDS_LIMIT, previousStockOnHand, quantity, ex));
    }
  }

  private StockCardLineItemReason determineReasonByQuantity(int previousStockOnHand) {
    if (quantity > previousStockOnHand) {
      return physicalCredit();
    } else if (quantity < previousStockOnHand) {
      return physicalDebit();
    } else {
      return physicalBalance();
    }
  }

  private boolean isPhysicalInventory() {
    return source == null && destination == null && reason == null;
  }

  private boolean shouldIncrease() {
    boolean hasSource = source != null;
    boolean isCredit = reason != null && reason.getReasonType() == ReasonType.CREDIT;
    return hasSource || isCredit;
  }
}
