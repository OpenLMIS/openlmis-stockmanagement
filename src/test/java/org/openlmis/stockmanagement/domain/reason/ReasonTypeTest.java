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


import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

public class ReasonTypeTest {

  @Test
  public void shouldNotFindReasonType() throws Exception {
    assertThat(ReasonType.fromString(null), is(nullValue()));
    assertThat(ReasonType.fromString(""), is(nullValue()));
    assertThat(ReasonType.fromString("     "), is(nullValue()));
    assertThat(ReasonType.fromString(RandomStringUtils.random(10)), is(nullValue()));
  }

  @Test
  public void shouldFindReasonType() throws Exception {
    Stream.of(ReasonType.values())
        .forEach(val -> assertThat(ReasonType.fromString(val.toString()), is(equalTo(val))));
    Stream.of(ReasonType.values())
        .forEach(val -> assertThat(
            ReasonType.fromString(val.toString().toLowerCase(ENGLISH)), is(equalTo(val))
        ));
    Stream.of(ReasonType.values())
        .forEach(val -> assertThat(
            ReasonType.fromString(val.toString().toUpperCase(ENGLISH)), is(equalTo(val))
        ));
  }

  @Test
  public void shouldCheckIfCreditIsNegative() {
    assertFalse(ReasonType.CREDIT.isNegative());
  }

  @Test
  public void shouldCheckIfDebitIsNegative() {
    assertTrue(ReasonType.DEBIT.isNegative());
  }
}
