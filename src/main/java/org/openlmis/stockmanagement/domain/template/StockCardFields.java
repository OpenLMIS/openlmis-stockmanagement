package org.openlmis.stockmanagement.domain.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_card_fields", schema = "stockmanagement")
public class StockCardFields extends BaseEntity {

  @ManyToOne()
  @JoinColumn()
  private StockCardTemplate stockCardTemplate;

  @ManyToOne()
  @JoinColumn()
  private AvailableStockCardFields availableStockCardFields;

  @Column(nullable = false)
  private Boolean isDisplayed = false;

  @Column(nullable = false)
  private Integer displayOrder = 0;
}
