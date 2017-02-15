package org.openlmis.stockmanagement.domain.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.adjustment.ReasonType;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static java.util.Arrays.asList;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({
        "stockCard", "originEvent",
        "source", "destination",
        "noticedDate", "savedDate",
        "userId"})
@Table(name = "stock_card_line_items", schema = "stockmanagement")
public class StockCardLineItem extends BaseEntity {

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockCard stockCard;

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column(nullable = false)
  private Integer quantity;

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
  private ZonedDateTime noticedDate;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime savedDate;

  @Column(nullable = false)
  private UUID userId;

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
  public static List<StockCardLineItem> createLineItemsFrom(
          StockEventDto eventDto, StockCard stockCard, UUID savedEventId, UUID userId)
          throws InstantiationException, IllegalAccessException {
    StockCardLineItem lineItem = new StockCardLineItem(
            stockCard,
            fromId(savedEventId, StockEvent.class),
            eventDto.getQuantity(),
            fromId(eventDto.getReasonId(), StockCardLineItemReason.class),
            eventDto.getSourceFreeText(), eventDto.getDestinationFreeText(),
            eventDto.getDocumentNumber(), eventDto.getReasonFreeText(), eventDto.getSignature(),
            fromId(eventDto.getSourceId(), Node.class),
            fromId(eventDto.getDestinationId(), Node.class),
            eventDto.getOccurredDate(), eventDto.getNoticedDate(), ZonedDateTime.now(), userId);
    stockCard.getLineItems().add(lineItem);
    return asList(lineItem);
  }

  /**
   * Calculate stock on hand with previous stock on hand.
   *
   * @param previousStockOnHand previous stock on hand.
   * @return calculated stock on hand.
   */
  public int calculateStockOnHand(int previousStockOnHand) {
    if (shouldIncrease()) {
      return previousStockOnHand + quantity;
    } else if (shouldDecrease()) {
      return previousStockOnHand - quantity;
    } else {
      return previousStockOnHand;
    }
  }

  private boolean shouldDecrease() {
    boolean hasDestination = destination != null;
    boolean isDebit = reason != null && reason.getReasonType() == ReasonType.DEBIT;
    return hasDestination || isDebit;
  }

  private boolean shouldIncrease() {
    boolean hasSource = source != null;
    boolean isCredit = reason != null && reason.getReasonType() == ReasonType.CREDIT;
    return hasSource || isCredit;
  }
}
