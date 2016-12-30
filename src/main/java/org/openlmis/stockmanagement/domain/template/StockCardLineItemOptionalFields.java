package org.openlmis.stockmanagement.domain.template;

import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_card_line_item_optional_fields", schema = "stockmanagement")
public class StockCardLineItemOptionalFields extends BaseEntity {

  @Column(nullable = false)
  private Boolean documentNumber;

  @Column(nullable = false)
  private Boolean receivedFrom;

  @Column(nullable = false)
  private Boolean issuedTo;

  @Column(nullable = false)
  private Boolean adjustmentReason;

  @Column(nullable = false)
  private Boolean pricePerUnit;

  @Column(nullable = false)
  private Boolean signature;
}
