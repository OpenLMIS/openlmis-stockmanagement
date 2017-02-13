package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramDto {
  private UUID id;
  private String code;
  private String name;
  private String description;
  private Boolean active;
  private Boolean periodsSkippable;
  private Boolean showNonFullSupplyTab;
}
