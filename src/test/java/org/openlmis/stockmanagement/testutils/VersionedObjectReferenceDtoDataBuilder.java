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

import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.dto.referencedata.VersionObjectReferenceDto;

public class VersionedObjectReferenceDtoDataBuilder {
  private UUID id;
  private String serviceUrl;
  private String path;
  private Long versionNumber;

  /**
   * Creates builder for creating new instance of {@link ObjectReferenceDto}.
   */
  public VersionedObjectReferenceDtoDataBuilder() {
    id = UUID.randomUUID();
    serviceUrl = "https://openlmis/";
    path = "api/resource";
    versionNumber = Long.parseLong("1");
  }

  /**
   * Creates new instance of {@link ObjectReferenceDto} with properties.
   * @return created object reference.
   */
  public VersionObjectReferenceDto build() {
    return new VersionObjectReferenceDto(id, serviceUrl, path, versionNumber);
  }

  public VersionedObjectReferenceDtoDataBuilder withPath(String path) {
    this.path = path;
    return this;
  }

  public VersionedObjectReferenceDtoDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }
}
