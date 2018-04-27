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
