package org.openlmis.stockmanagement.domain.movement;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;


@Entity
@Data
@Table(name = "nodes", schema = "stockmanagement")
public class Node extends BaseEntity {
  @Column(nullable = false)
  @Type(type = PG_UUID)
  UUID referenceId;

  @Column(nullable = false)
  boolean isRefDataFacility;
}
