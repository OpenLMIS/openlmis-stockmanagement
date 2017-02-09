package org.openlmis.stockmanagement.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProgramOrderableDto {
  private UUID programId;
  private UUID orderableId;
  private UUID orderableDisplayCategoryId;
  private String orderableCategoryDisplayName;
  private Integer orderableCategoryDisplayOrder;
  private Boolean active;
  private Boolean fullSupply;
  private Integer displayOrder;
}
