package org.openlmis.stockmanagement.domain.template;

import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "available_stock_card_fields", schema = "stockmanagement")
public class AvailableStockCardFields extends BaseEntity {
  @Column(nullable = false)
  private String name;
}
