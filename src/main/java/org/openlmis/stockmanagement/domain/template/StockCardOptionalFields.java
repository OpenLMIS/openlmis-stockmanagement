package org.openlmis.stockmanagement.domain.template;

import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_card_optional_fields", schema = "stockmanagement")
public class StockCardOptionalFields extends BaseEntity {

  @Column(nullable = false)
  private Boolean packSize;

  @Column(nullable = false)
  private Boolean donor;

  @Column(nullable = false)
  private Boolean maxMonthsOfStock;

  @Column(nullable = false)
  private Boolean minMonthsOfStock;
}
