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

package org.openlmis.stockmanagement.domain.reason;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public enum ReasonCategory {
  TRANSFER, ADJUSTMENT, PHYSICAL_INVENTORY;

  /**
   * Find a correct {@link ReasonCategory} instance based on the passed string. The method ignores
   * the case.
   *
   * @param arg string representation of one of reason category.
   * @return instance of {@link ReasonCategory} if the given string matches type; otherwise null.
   */
  public static ReasonCategory fromString(String arg) {
    for (ReasonCategory status : values()) {
      if (equalsIgnoreCase(arg, status.name())) {
        return status;
      }
    }

    return null;
  }
}
