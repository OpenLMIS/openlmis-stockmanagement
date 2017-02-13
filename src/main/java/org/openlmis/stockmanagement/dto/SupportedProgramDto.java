package org.openlmis.stockmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class SupportedProgramDto {
  private UUID id;
  private String code;
  private String name;
  private String description;
  private boolean programActive;
  private boolean periodsSkippable;
  private boolean showNonFullSupplyTab;
  private boolean supportActive;
  private LocalDate supportStartDate;
}
