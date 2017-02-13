package org.openlmis.stockmanagement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class FacilityDto {
  private UUID id;
  private String code;
  private String name;
  private String description;
  private Boolean active;
  private LocalDate goLiveDate;
  private LocalDate goDownDate;
  private String comment;
  private Boolean enabled;
  private Boolean openLmisAccessible;
  private List<SupportedProgramDto> supportedPrograms;
  private GeographicZoneDto geographicZone;
  private FacilityTypeDto type;
}
