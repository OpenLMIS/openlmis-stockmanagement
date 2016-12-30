package org.openlmis.stockmanagement.domain.template;

import lombok.Getter;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_card_line_item_optional_fields", schema = "stockmanagement")
public class StockCardLineItemOptionalFields extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean documentNumber = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean receivedFrom = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean issuedTo = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean adjustmentReason = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean pricePerUnit = false;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean signature = false;
}
