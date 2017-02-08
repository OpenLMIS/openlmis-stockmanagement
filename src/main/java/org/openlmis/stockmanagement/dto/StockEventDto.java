package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.movement.Node;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockEventDto {

  private Integer quantity;
  private UUID reasonId;

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
  public StockEvent toEvent(UUID userId)
          throws InstantiationException, IllegalAccessException {

    return new StockEvent(quantity, fromId(reasonId, StockCardLineItemReason.class),
            facilityId, programId, orderableId, userId,
            fromId(sourceId, Node.class), fromId(destinationId, Node.class),
            occurredDate, noticedDate, ZonedDateTime.now(),
            signature, reasonFreeText, sourceFreeText, destinationFreeText, documentNumber);
  }

  public boolean hasAlternativeStockCardIdentifier() {
    return programId != null && facilityId != null & orderableId != null;
  }

  public boolean hasStockCardIdentifier() {
    return stockCardId != null;
  }
}
