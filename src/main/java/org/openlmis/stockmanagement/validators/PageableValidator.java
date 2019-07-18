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

package org.openlmis.stockmanagement.validators;

import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.util.PageableRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A validator for {@link PageableRequestContext} object.
 */
@Component
public class PageableValidator implements Validator {

  private static final String SIZE = "size";

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link PageableRequestContext} class definition.
   *      Otherwise false.
   */

  public boolean supports(Class<?> clazz) {
    return PageableRequestContext.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance
   * of {@link PageableRequestContext} class.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */

  public void validate(Object target, Errors errors) {

    PageableRequestContext ctx = (PageableRequestContext) target;

    if (ctx.getPage() != null) {
      if (ctx.getSize() == null) {
        errors.rejectValue(SIZE, MessageKeys.ERROR_SIZE_NULL, MessageKeys.ERROR_SIZE_NULL);
      } else if (ctx.getSize() < 1) {
        errors.rejectValue(SIZE, MessageKeys.ERROR_SIZE_NOT_POSITIVE,
                MessageKeys.ERROR_SIZE_NOT_POSITIVE);
      }
    }
  }
}


