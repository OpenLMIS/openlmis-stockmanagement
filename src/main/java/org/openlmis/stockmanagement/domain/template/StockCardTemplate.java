package org.openlmis.stockmanagement.domain.template;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Data
@Table(name = "stock_card_template", schema = "stockmanagement")
public class StockCardTemplate extends BaseEntity {

  @Column(nullable = false)
  @Type(type = PG_UUID)
  private UUID programId;

  @Column(nullable = false)
  @Type(type = PG_UUID)
  private UUID facilityTypeId;

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  private StockCardOptionalFields stockCardOptionalFields =
      new StockCardOptionalFields();

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  private StockCardLineItemOptionalFields stockCardLineItemOptionalFields =
      new StockCardLineItemOptionalFields();
}
