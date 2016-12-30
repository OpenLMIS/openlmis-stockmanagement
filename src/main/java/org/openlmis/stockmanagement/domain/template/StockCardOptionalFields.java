package org.openlmis.stockmanagement.domain.template;

import lombok.Getter;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_card_optional_fields", schema = "stockmanagement")
public class StockCardOptionalFields extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean packSize = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean donor = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean maxMonthsOfStock = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean minMonthsOfStock = false;
}
