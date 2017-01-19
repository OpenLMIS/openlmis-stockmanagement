package org.openlmis.stockmanagement.domain.movement;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Data
@Table(name = "valid_destination_assignments", schema = "stockmanagement")
public class ValidDestinationAssignment extends BaseEntity {
  @Column(nullable = false)
  @Type(type = PG_UUID)
  UUID programId;

  @Column(nullable = false)
  @Type(type = PG_UUID)
  UUID facilityTypeId;

  @ManyToOne()
  @JoinColumn(nullable = false)
  Node node;
}
