package org.openlmis.stockmanagement.domain.template;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.Type;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;

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

  @LazyCollection(FALSE)
  @OneToMany(cascade = ALL, mappedBy = "stockCardTemplate")
  private List<StockCardFields> stockCardFields = new ArrayList<>();

  @LazyCollection(FALSE)
  @OneToMany(cascade = ALL, mappedBy = "stockCardTemplate")
  private List<StockCardLineItemFields> stockCardLineItemFields = new ArrayList<>();
}
