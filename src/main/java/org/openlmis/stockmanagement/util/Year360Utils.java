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

package org.openlmis.stockmanagement.util;

import static java.lang.Math.min;

import java.time.LocalDate;

/**
 * A set of utilities to do date calculations based on 360 days calendar.
 */
public class Year360Utils {
  public static final int DAYS_IN_YEAR = 360;
  public static final int DAYS_IN_MONTH = 30;

  /**
   * Hidden constructor.
   */
  private Year360Utils() {
  }

  /**
   * Return the number of days between two given dates in 360-day calendar in EU convention.
   *
   * @param startDateInclusive the start date, not null
   * @param endDateExclusive   the end date, exclusive, not null
   * @return the number of days according to EU convention, never null
   */
  public static long getDaysBetweenEu(LocalDate startDateInclusive,
      LocalDate endDateExclusive) {
    long startYear = startDateInclusive.getYear();
    long startMonth = startDateInclusive.getMonthValue();
    long startDayLimited = min(startDateInclusive.getDayOfMonth(), DAYS_IN_MONTH);
    long endYear = endDateExclusive.getYear();
    long endMonth = endDateExclusive.getMonthValue();
    long endDayLimited = min(endDateExclusive.getDayOfMonth(), DAYS_IN_MONTH);

    long daysByYear = (endYear - startYear) * DAYS_IN_YEAR;
    long daysByMonths = (endMonth - startMonth - 1) * DAYS_IN_MONTH;
    long daysByDays = (DAYS_IN_MONTH - startDayLimited) + endDayLimited;

    return daysByYear + daysByMonths + daysByDays;
  }

  /**
   * Return the number of days between two given dates in 360-day calendar in US convention.
   *
   * @param startDateInclusive the start date, not null
   * @param endDateExclusive   the end date, exclusive, not null
   * @return the number of days according to US convention, never null
   */
  public static long getDaysBetweenUs(LocalDate startDateInclusive,
      LocalDate endDateExclusive) {
    long startYear = startDateInclusive.getYear();
    long startMonth = startDateInclusive.getMonthValue();
    long startDay = startDateInclusive.getDayOfMonth();

    long endYear = endDateExclusive.getYear();
    long endMonth = endDateExclusive.getMonthValue();
    long endDay = endDateExclusive.getDayOfMonth();

    if (endDay == DAYS_IN_MONTH + 1 && startDay < DAYS_IN_MONTH) {
      endDay = 1;
      endMonth++;
    }

    long startDayLimited = min(startDay, DAYS_IN_MONTH);
    long endDayLimited = min(endDay, DAYS_IN_MONTH);

    long daysByYear = (endYear - startYear) * DAYS_IN_YEAR;
    long daysByMonths = (endMonth - startMonth - 1) * DAYS_IN_MONTH;
    long daysByDays = (DAYS_IN_MONTH - startDayLimited) + endDayLimited;

    return daysByYear + daysByMonths + daysByDays;
  }
}
