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

import static java.time.ZoneId.of;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class DatesUtil {

  /**
   * Create a zoned date time.
   *
   * @return zoned date time.
   */
  public static ZonedDateTime getBaseDateTime() {
    return ZonedDateTime.of(2017, 2, 14, 15, 20, 0, 0, of("UTC"));
  }

  /**
   * Create a zoned date time.
   *
   * @return zoned date time.
   */
  public static LocalDate getBaseDate() {
    return LocalDate.of(2017, 2, 14);
  }

  /**
   * Shift base date by one hour earlier.
   *
   * @param baseDate base date.
   * @return shifted date time.
   */
  public static ZonedDateTime oneHourEarlier(ZonedDateTime baseDate) {
    return baseDate.minusHours(1);
  }

  /**
   * Shift base date by one day later.
   *
   * @param baseDate base date.
   * @return shifted date time.
   */
  public static ZonedDateTime oneDayLater(ZonedDateTime baseDate) {
    return baseDate.plusDays(1);
  }

  /**
   * Shift base date by one hour later.
   *
   * @param baseDate base date.
   * @return shifted date time.
   */
  public static ZonedDateTime oneHourLater(ZonedDateTime baseDate) {
    return baseDate.plusHours(1);
  }
}
