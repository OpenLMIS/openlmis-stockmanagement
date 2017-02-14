package org.openlmis.stockmanagement.testutils;

import java.time.ZonedDateTime;

import static java.time.ZoneId.of;

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
