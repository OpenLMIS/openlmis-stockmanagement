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

package org.openlmis.stockmanagement.web;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;

public abstract class BaseController {
  private final XLogger extLogger = XLoggerFactory.getXLogger(getClass());

  Profiler getProfiler(String name, Object... entryArgs) {
    extLogger.entry(entryArgs);

    Profiler profiler = new Profiler(name);
    profiler.setLogger(extLogger);

    return profiler;
  }

  <T> T stopProfiler(Profiler profiler, T exitArg) {
    profiler.stop().log();
    extLogger.exit(exitArg);

    return exitArg;
  }

}
