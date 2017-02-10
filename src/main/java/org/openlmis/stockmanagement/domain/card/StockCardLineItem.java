package org.openlmis.stockmanagement.domain.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
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

import static java.util.Arrays.asList;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
   * @param eventDto  stock eventDto.
   * @param stockCard the card that this line item belongs to.
   * @param userId    user who performed the operation.  @return created line item.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  public static List<StockCardLineItem> createLineItemsFrom(
          StockEventDto eventDto, StockCard stockCard, UUID userId)
          throws InstantiationException, IllegalAccessException {
    StockCardLineItem lineItem = new StockCardLineItem(
            stockCard,
            stockCard.getOriginEvent(),
            eventDto.getQuantity(),
            fromId(eventDto.getReasonId(), StockCardLineItemReason.class),
            eventDto.getSourceFreeText(), eventDto.getDestinationFreeText(),
            eventDto.getDocumentNumber(), eventDto.getReasonFreeText(), eventDto.getSignature(),
            fromId(eventDto.getSourceId(), Node.class),
            fromId(eventDto.getDestinationId(), Node.class),
            eventDto.getOccurredDate(), eventDto.getNoticedDate(), ZonedDateTime.now(), userId);
    return asList(lineItem);
  }

}
