package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockEventDto {

  private Integer quantity;
  private StockCardLineItemReason reason;

  private UUID stockCardId;

  private UUID facilityId;
  private UUID programId;
  private UUID orderableId;

  private UUID sourceId;
  private UUID destinationId;

  private ZonedDateTime occurredDate;
  private ZonedDateTime noticedDate;

  private String signature;

  private String reasonFreeText;
  private String sourceFreeText;
  private String destinationFreeText;

  private String documentNumber;
}
