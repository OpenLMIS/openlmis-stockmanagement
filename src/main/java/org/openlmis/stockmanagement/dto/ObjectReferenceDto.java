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

package org.openlmis.stockmanagement.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public final class ObjectReferenceDto extends BaseDto {

  private static final String SEPARATOR = "/";

  @Getter
  @Setter
  private String href;

  /**
   * Returns new object reference.
   *
   * @param path resource path
   * @param id   object id
   */
  public ObjectReferenceDto(String serviceUrl, String path, UUID id) {
    super(id);
    this.href = StringUtils.joinWith(SEPARATOR, serviceUrl + path, id);
  }
}
