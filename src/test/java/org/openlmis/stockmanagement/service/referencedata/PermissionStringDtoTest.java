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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@AllArgsConstructor
public class PermissionStringDtoTest {
  private String permissionString;
  private String rightName;
  private UUID facilityId;
  private UUID programId;

  /**
   * Generates data for tests. It generates 10 supervision rights, 10 fulfillment rights and 10
   * general rights.
   */
  @Parameterized.Parameters(name = "{index}: {0}: {1}|{2}|{3}")
  public static Collection<Object[]> data() {
    int size = 30;
    Object[][] data = new Object[size][4];

    for (int i = 0; i < size; ++i) {
      String rightName = RandomStringUtils.randomAlphabetic(5).toUpperCase(Locale.ENGLISH);
      UUID facility = i < 20 ? UUID.randomUUID() : null;
      UUID program = i < 10 ? UUID.randomUUID() : null;

      Object[] row = new Object[4];
      row[0] = String.format(
          "%s%s%s", rightName, null == facility ? "" : "|" + facility,
          null == program ? "" : "|" + program
      );
      row[1] = rightName;
      row[2] = facility;
      row[3] = program;

      data[i] = row;
    }

    return Arrays.asList(data);
  }

  @Test
  public void shouldParse() throws Exception {
    PermissionStringDto parsed = PermissionStringDto.from(permissionString);
    assertThat(parsed.getRightName(), equalTo(rightName));
    assertThat(parsed.getFacilityId(), equalTo(facilityId));
    assertThat(parsed.getProgramId(), equalTo(programId));
    assertThat(parsed.toString(), equalTo(permissionString));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(PermissionStringDto.class)
        .suppress(Warning.NONFINAL_FIELDS) // we can't make fields as final in DTO
        .verify();
  }

}