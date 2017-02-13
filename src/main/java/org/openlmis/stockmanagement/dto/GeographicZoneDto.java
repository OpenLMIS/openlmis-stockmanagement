package org.openlmis.stockmanagement.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GeographicZoneDto {
  private UUID id;
  private String code;
  private String name;
  private GeographicLevelDto level;
  private GeographicZoneDto parent;
}
