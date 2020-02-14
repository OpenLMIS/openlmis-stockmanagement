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

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Component;

/**
 * All implementations of this interface is supposed to be independent. Meaning that it should not
 * assume any specific execution order.
 * An implementation of this interface should work correctly no matter which other validator is ran
 * before or after it.
 */
@Component
public interface StockEventValidator {
  XLogger XLOGGER = XLoggerFactory.getXLogger(StockEventValidator.class);

  void validate(StockEventDto stockEventDto);
}
