package org.openlmis.stockmanagement.domain.template;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "stock_card_optional_fields", schema = "stockmanagement")
public class StockCardOptionalFields extends BaseEntity {

  @Column(nullable = false)
  private Boolean packSize = false;
  private Integer packSizeDisplayOrder;

  @Column(nullable = false)
  private Boolean donor = false;
  private Integer donorDisplayOrder;

  @Column(nullable = false)
  private Boolean maxMonthsOfStock = false;
  private Integer maxMonthsOfStockDisplayOrder;

  @Column(nullable = false)
  private Boolean minMonthsOfStock = false;
  private Integer minMonthsOfStockDisplayOrder;
}
