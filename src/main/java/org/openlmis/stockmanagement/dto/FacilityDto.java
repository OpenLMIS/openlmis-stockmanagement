package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.movement.Organization;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  /**
   * Create facility dto from organization.
   *
   * @param organization organization.
   * @return facility dto.
   */
  public static FacilityDto createFrom(Organization organization) {
    return FacilityDto.builder()
            .name(organization.getName())
            .build();
  }
}
