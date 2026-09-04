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

package org.openlmis.stockmanagement.exception;

import java.util.List;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;

/**
 * Thrown when one or more line items selected for cancellation fail validation. Carries the
 * per-line errors so the error handler can return which lines are blocking the action and why.
 */
public class StockEventCancellationException extends BaseMessageException {

  private final String messageKey;
  private final transient List<StockEventCancellationLineErrorDto> lineErrors;

  /**
   * Creates a new exception.
   *
   * @param messageKey the general message key describing the failure.
   * @param lineErrors the per-line validation errors.
   */
  public StockEventCancellationException(String messageKey,
      List<StockEventCancellationLineErrorDto> lineErrors) {
    super(messageKey);
    this.messageKey = messageKey;
    this.lineErrors = lineErrors;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public List<StockEventCancellationLineErrorDto> getLineErrors() {
    return lineErrors;
  }
}
