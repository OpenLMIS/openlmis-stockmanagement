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

package org.openlmis.stockmanagement.service.referencedata;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Object representation of single permission string.
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionStringDto {
  private String rightName;
  private UUID facilityId;
  private UUID programId;

  /**
   * Parses a collection of string representation of permissionString to set with object
   * representation.
   *
   * @param permissionStrings a collection of string representation
   * @return a set with {@link PermissionStringDto} instances
   */
  public static Set<PermissionStringDto> from(Collection<String> permissionStrings) {
    return permissionStrings
        .parallelStream()
        .map(PermissionStringDto::from)
        .collect(Collectors.toSet());
  }

  /**
   * Parses string representation of permissionString to object representation.
   *
   * @param permissionString string representation
   * @return {@link PermissionStringDto}
   */
  public static PermissionStringDto from(String permissionString) {
    String[] elements = splitByWholeSeparator(permissionString, "|");
    String rightName = elements[0];
    UUID facilityId = elements.length > 1 ? UUID.fromString(elements[1]) : null;
    UUID programId = elements.length > 2 ? UUID.fromString(elements[2]) : null;

    return create(rightName, facilityId, programId);
  }

  public static PermissionStringDto create(String rightName, UUID facilityId, UUID programId) {
    return new PermissionStringDto(rightName, facilityId, programId);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(rightName);

    if (null != facilityId) {
      builder = builder.append("|").append(facilityId);
    }

    if (null != programId) {
      builder = builder.append("|").append(programId);
    }

    return builder.toString();
  }

}
