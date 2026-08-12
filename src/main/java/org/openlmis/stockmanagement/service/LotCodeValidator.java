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

import java.util.regex.Pattern;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.stereotype.Component;

/**
 * Validates a lot code against the GS1 Application Identifier (10) contract: a maximum length and
 * the GS1 invariant character set. Kept in sync with the referencedata Lot domain bound and the
 * barcode scan parser's allowed set.
 */
@Component
public class LotCodeValidator {

  private static final int MAX_LENGTH = 20;
  private static final Pattern ALLOWED_CHARACTERS =
      Pattern.compile("[!\"%&'()*+,\\-./0-9:;<=>?A-Z_a-z]*");

  /**
   * Validates the given lot code.
   *
   * @param lotCode the lot code to validate
   * @throws ValidationMessageException if the code is null, empty, longer than the maximum, or
   *     contains characters outside the GS1 set
   */
  public void validate(String lotCode) {
    if (lotCode == null
        || lotCode.isEmpty()
        || lotCode.length() > MAX_LENGTH
        || !ALLOWED_CHARACTERS.matcher(lotCode).matches()) {
      throw new ValidationMessageException(
          new Message(MessageKeys.ERROR_EVENT_LOT_CODE_INVALID, lotCode));
    }
  }
}
