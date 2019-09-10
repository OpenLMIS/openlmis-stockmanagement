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

package org.openlmis.stockmanagement.testutils;

import java.util.UUID;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.dto.referencedata.GeographicLevelDto;
import org.openlmis.stockmanagement.dto.referencedata.GeographicZoneDto;

@NoArgsConstructor
public class GeographicZoneDtoDataBuilder {

  private UUID id;
  private String code;
  private String name;
  private GeographicLevelDto level;
  private Integer catchmentPopulation;
  private Double latitude;
  private Double longitude;

  private GeographicZoneDto parent;

  public GeographicZoneDtoDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public GeographicZoneDtoDataBuilder withLevel(GeographicLevelDto level) {
    this.level = level;
    return this;
  }

  public GeographicZoneDtoDataBuilder withParent(GeographicZoneDto parent) {
    this.parent = parent;
    return this;
  }

  /**
   * Creates new instance of {@link GeographicZoneDto} with properties.
   *
   * @return created geographic zone dto.
   */
  public GeographicZoneDto build() {
    GeographicZoneDto geographicZoneDto = new GeographicZoneDto(code, name, level,
        catchmentPopulation, latitude, longitude, parent);
    geographicZoneDto.setId(id);
    return geographicZoneDto;
  }
}
