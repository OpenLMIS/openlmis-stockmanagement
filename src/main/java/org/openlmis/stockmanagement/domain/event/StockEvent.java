package org.openlmis.stockmanagement.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockEvent {
  private Integer quantity;
  private StockCardLineItemReason reason;

  private UUID stockCardId;

  private UUID facilityId;
  private UUID programId;
  private UUID orderableId;

  private UUID userId;

  private UUID sourceId;
  private UUID destinationId;

  private Date occurredDate;
  private Date noticedDate;
  private Date savedDate;

  private String signature;

  private String reasonFreeText;
  private String sourceFreeText;
  private String destinationFreeText;
}
