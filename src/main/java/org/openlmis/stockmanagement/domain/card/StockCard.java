package org.openlmis.stockmanagement.domain.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_cards", schema = "stockmanagement")
public class StockCard extends BaseEntity {
  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column
  private UUID facilityId;
  private UUID programId;
  private UUID orderableId;

  public static StockCard createFrom(StockEventDto stockEventDto, StockEvent savedEvent) {
    return new StockCard(savedEvent, stockEventDto.getFacilityId(),
            stockEventDto.getProgramId(), stockEventDto.getOrderableId());
  }
}
