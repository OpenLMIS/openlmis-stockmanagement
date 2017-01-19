package org.openlmis.stockmanagement.domain.movement;

import lombok.Data;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "organizations", schema = "stockmanagement")
public class Organization extends BaseEntity {
  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  String name;
}
