package org.openlmis.stockmanagement.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ApprovedProductDto {
  private UUID id;
  private ProgramOrderableDto programOrderable;
}
