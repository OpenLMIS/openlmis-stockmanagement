package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.event.StockEvent;

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

  /**
   * Convert dto to jpa model.
   *
   * @param userId user id.
   * @return the converted jpa model object.
   */
  public StockEvent toEvent(UUID userId) {
    return new StockEvent(quantity, reason,
            stockCardId, facilityId, programId, orderableId,
            userId,
            sourceId, destinationId,
            occurredDate, noticedDate, ZonedDateTime.now(),
            signature, reasonFreeText, sourceFreeText, destinationFreeText, documentNumber);
  }
}
