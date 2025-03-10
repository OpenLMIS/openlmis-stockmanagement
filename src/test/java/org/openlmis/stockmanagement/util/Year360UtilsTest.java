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

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class Year360UtilsTest {
  // As calculated by DAYS360 function in Google Sheets
  private static final List<TestDataRow> COMMON_TEST_DATA = Arrays
      .asList(new TestDataRow("2025-05-03", "2025-05-03", 0, 0),
          new TestDataRow("2025-05-03", "2025-05-09", 6, 6),
          new TestDataRow("2025-05-03", "2025-06-09", 36, 36),
          new TestDataRow("2025-04-03", "2025-05-09", 36, 36),
          new TestDataRow("2025-02-03", "2025-03-03", 30, 30),
          new TestDataRow("2025-01-03", "2025-02-03", 30, 30),
          new TestDataRow("2025-01-03", "2025-05-03", 120, 120),
          new TestDataRow("2025-01-31", "2025-02-03", 3, 3),
          new TestDataRow("2025-02-03", "2025-03-31", 58, 57),
          new TestDataRow("2025-01-31", "2025-03-31", 60, 60),
          new TestDataRow("2025-02-01", "2026-04-01", 420, 420),
          new TestDataRow("2025-02-03", "2026-03-31", 418, 417),
          new TestDataRow("2019-02-01", "2019-02-28", 27, 27));

  @Test
  public void testUsCalculation() {
    for (TestDataRow row : COMMON_TEST_DATA) {
      assertEquals(row.usCount, Year360Utils.getDaysBetweenUs(row.startDate, row.endDate));
    }
  }

  @Test
  public void testEuCalculation() {
    for (TestDataRow row : COMMON_TEST_DATA) {
      assertEquals(row.euCount, Year360Utils.getDaysBetweenEu(row.startDate, row.endDate));
    }
  }

  private static class TestDataRow {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long usCount;
    private final long euCount;

    TestDataRow(String startDate,
        String endDate,
        long usCount,
        long euCount) {
      this.startDate = LocalDate.parse(startDate);
      this.endDate = LocalDate.parse(endDate);
      this.usCount = usCount;
      this.euCount = euCount;
    }
  }
}
