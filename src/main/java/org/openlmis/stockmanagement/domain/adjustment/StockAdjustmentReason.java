package org.openlmis.stockmanagement.domain.adjustment;

import lombok.Data;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "stock_adjustment_reasons", schema = "stockmanagement")
public class StockAdjustmentReason extends BaseEntity {

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String description;

  @Column(nullable = false, columnDefinition = "text")
  @Enumerated(value = EnumType.STRING)
  private AdjustmentType adjustmentType;

  @Column(nullable = false)
  private Boolean isFreeTextAllowed = false;
}
