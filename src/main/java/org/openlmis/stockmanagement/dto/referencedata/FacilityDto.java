/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.stockmanagement.dto.referencedata;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.openlmis.stockmanagement.domain.sourcedestination.Organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
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
  //  private GeographicZoneDto geographicZone;
  //geographicZone is not used anywhere by stock management, we can take it back when needed
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
