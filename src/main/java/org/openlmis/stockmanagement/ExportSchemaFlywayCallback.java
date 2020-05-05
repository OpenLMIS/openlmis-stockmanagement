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

package org.openlmis.stockmanagement;

import static org.flywaydb.core.api.callback.Event.AFTER_MIGRATE;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.flywaydb.core.api.callback.BaseCallback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class ExportSchemaFlywayCallback extends BaseCallback {

  private static final XLogger XLOGGER = XLoggerFactory
      .getXLogger(ExportSchemaFlywayCallback.class);

  @Value("${spring.flyway.schemas}")
  private String schemaName;

  @Override
  public boolean supports(Event event, Context context) {
    return AFTER_MIGRATE.equals(event);
  }

  @Override
  public void handle(Event event, Context context) {
    if (AFTER_MIGRATE.equals(event)) {
      XLOGGER.entry(context.getConnection());

      XLOGGER.info("After migrations, exporting db schema");

      int exitCode = 0;
      try {
        schemaName = Optional.ofNullable(schemaName).orElse("stockmanagement");

        Process proc = Runtime.getRuntime().exec("/app/export_schema.sh " + schemaName);

        StreamGobbler streamGobbler =
            new StreamGobbler(proc.getInputStream(), XLOGGER::info);
        Executors.newSingleThreadExecutor().submit(streamGobbler);

        exitCode = proc.waitFor();
      } catch (Exception ex) {
        XLOGGER.warn("Exporting db schema failed with message: " + ex);
      }

      XLOGGER.exit(exitCode);
    }
  }

  private static class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;

    StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines()
          .forEach(consumer);
    }
  }
}
