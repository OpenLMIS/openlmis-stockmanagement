package org.openlmis.stockmanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "just_testing_erd_generation", schema = "stockmanagement")
@NoArgsConstructor
@AllArgsConstructor
public class JustTestingErdGeneration {

  @Id
  private String id;

  @Column(nullable = false)
  @Getter
  @Setter
  private String someField;

  @Column(nullable = false)
  @Getter
  @Setter
  private String someOtherField;
}
