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

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.extension.point.StockEventPostProcessor;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

@Component("DefaultStockEventPostProcessor")
public class DefaultStockEventPostProcessor implements StockEventPostProcessor {
  private static final XLogger XLOGGER = XLoggerFactory
            .getXLogger(DefaultStockEventPostProcessor.class);

  @Override
  public void process(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);
    Profiler profiler = new Profiler("DEFAULT_STOCK_EVENT_POST_PROCESSOR");
    profiler.setLogger(XLOGGER);

    XLOGGER.info("This default processor does nothing, it is just a placeholder");

    profiler.stop().log();
    XLOGGER.exit();
  }
}
