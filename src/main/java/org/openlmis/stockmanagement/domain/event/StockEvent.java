package org.openlmis.stockmanagement.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_events", schema = "stockmanagement")
public class StockEvent extends BaseEntity {

  @Column(nullable = false)
  private Integer quantity;

  @ManyToOne()
  @JoinColumn()
  private StockCardLineItemReason reason;

  private UUID stockCardId;

  private UUID facilityId;
  private UUID programId;
  private UUID orderableId;

  @Column(nullable = false)
  private UUID userId;

  private UUID sourceId;
  private UUID destinationId;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime occurredDate;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime noticedDate;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime savedDate;

  private String signature;

  private String reasonFreeText;
  private String sourceFreeText;
  private String destinationFreeText;

  private String documentNumber;
}
