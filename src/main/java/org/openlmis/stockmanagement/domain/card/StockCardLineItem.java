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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.ExtraDataConverter;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

@Entity
@Data
@Builder
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

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockCard stockCard;

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  private Map<String, String> extraData;

  @ManyToOne()
  @JoinColumn()
  private StockCardLineItemReason reason;

  private String sourceFreeText;
  private String destinationFreeText;
  private String documentNumber;
  private String reasonFreeText;
  private String signature;

  @ManyToOne()
  @JoinColumn()
  private Node source;

  @ManyToOne()
  @JoinColumn()
  private Node destination;

  @Column(nullable = false, columnDefinition = "timestamp")
  @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  private ZonedDateTime occurredDate;

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
  private List<StockAdjustment> stockAdjustments;

  /**
   * Create line item from eventDto.
   *
   * @param eventDto     stock eventDto.
   * @param stockCard    the card that this line item belongs to.
   * @param savedEventId saved event id.
   * @param userId       user who performed the operation.  @return created line item.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  public static StockCardLineItem createLineItemFrom(
      StockEventDto eventDto, StockEventLineItem eventLineItem,
      StockCard stockCard, UUID savedEventId, UUID userId)
      throws InstantiationException, IllegalAccessException {
    StockCardLineItem cardLineItem = StockCardLineItem.builder()
        .stockCard(stockCard)
        .originEvent(fromId(savedEventId, StockEvent.class))

        .quantity(eventLineItem.getQuantity())
        .stockAdjustments(eventLineItem.getStockAdjustments() != null
            ? new ArrayList<>(eventLineItem.getStockAdjustments())
            : null)

        .occurredDate(eventLineItem.getOccurredDate())
        .processedDate(now())

        .reason(fromId(eventLineItem.getReasonId(), StockCardLineItemReason.class))
        .source(fromId(eventLineItem.getSourceId(), Node.class))
        .destination(fromId(eventLineItem.getDestinationId(), Node.class))

        .reasonFreeText(eventLineItem.getReasonFreeText())
        .sourceFreeText(eventLineItem.getSourceFreeText())
        .destinationFreeText(eventLineItem.getDestinationFreeText())

        .documentNumber(eventDto.getDocumentNumber())
        .signature(eventDto.getSignature())
        .userId(userId)

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
