package org.openlmis.stockmanagement.domain.template;

import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "available_stock_card_line_item_fields", schema = "stockmanagement")
public class AvailableStockCardLineItemFields extends BaseEntity {
  @Column(nullable = false)
  private String name;
}
