package org.openlmis.stockmanagement.domain.template;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "stock_card_template", schema = "stockmanagement")
public class StockCardTemplate extends BaseEntity {

  @Column(nullable = false)
  @Type(type = PG_UUID)
  @Getter
  @Setter
  private UUID programId;

  @Column(nullable = false)
  @Type(type = PG_UUID)
  @Getter
  @Setter
  private UUID facilityTypeId;

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @Getter
  @Setter
  private StockCardOptionalFields stockCardOptionalFields;

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @Getter
  @Setter
  private StockCardLineItemOptionalFields stockCardLineItemOptionalFields;

}
