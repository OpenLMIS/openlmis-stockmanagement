package org.openlmis.stockmanagement.domain.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
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
   * Create line item from event.
   *
   * @param event  stock event.
   * @param userId user who performed the operation.
   * @return created line item.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  public static List<StockCardLineItem> createFrom(StockEventDto event, UUID userId)
          throws InstantiationException, IllegalAccessException {
    StockCardLineItem lineItem = new StockCardLineItem(
            event.getQuantity(), fromId(event.getReasonId(), StockCardLineItemReason.class),
            event.getSourceFreeText(), event.getDestinationFreeText(),
            event.getDocumentNumber(), event.getReasonFreeText(), event.getSignature(),
            fromId(event.getSourceId(), Node.class), fromId(event.getDestinationId(), Node.class),
            event.getOccurredDate(), event.getNoticedDate(), ZonedDateTime.now(), userId);
    return asList(lineItem);
  }

}
