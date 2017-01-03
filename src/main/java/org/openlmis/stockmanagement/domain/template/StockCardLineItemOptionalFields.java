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
@Table(name = "stock_card_line_item_optional_fields", schema = "stockmanagement")
public class StockCardLineItemOptionalFields extends BaseEntity {

  @Column(nullable = false)
  private Boolean documentNumber = false;
  private Integer documentNumberDisplayOrder;

  @Column(nullable = false)
  private Boolean receivedFrom = false;
  private Integer receivedFromDisplayOrder;

  @Column(nullable = false)
  private Boolean issuedTo = false;
  private Integer issuedToDisplayOrder;

  @Column(nullable = false)
  private Boolean adjustmentReason = false;
  private Integer adjustmentReasonDisplayOrder;

  @Column(nullable = false)
  private Boolean pricePerUnit = false;
  private Integer pricePerUnitDisplayOrder;

  @Column(nullable = false)
  private Boolean signature = false;
  private Integer signatureDisplayOrder;
}
