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

package org.openlmis.stockmanagement.service;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_CODE_INVALID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;

public class LotCodeValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private final LotCodeValidator validator = new LotCodeValidator();

  @Test
  public void shouldAcceptCodeWithinLengthAndCharset() {
    validator.validate("ABC-123/xyz.01");
  }

  @Test
  public void shouldAcceptCodeAtMaxLength() {
    validator.validate("12345678901234567890");
  }

  @Test
  public void shouldRejectNullCode() {
    expectValidationError();

    validator.validate(null);
  }

  @Test
  public void shouldRejectEmptyCode() {
    expectValidationError();

    validator.validate("");
  }

  @Test
  public void shouldRejectCodeLongerThanMax() {
    expectValidationError();

    validator.validate("123456789012345678901");
  }

  @Test
  public void shouldRejectCodeWithSpace() {
    expectValidationError();

    validator.validate("AB CD");
  }

  @Test
  public void shouldRejectCodeWithDisallowedCharacter() {
    expectValidationError();

    validator.validate("ABC#123");
  }

  private void expectValidationError() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_LOT_CODE_INVALID);
  }
}
