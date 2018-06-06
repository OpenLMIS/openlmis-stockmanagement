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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Optional;
import org.flywaydb.core.api.callback.BaseFlywayCallback;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class ExportSchemaFlywayCallback extends BaseFlywayCallback {

  private static final XLogger XLOGGER = XLoggerFactory
      .getXLogger(ExportSchemaFlywayCallback.class);

  @Value("${flyway.schemas}")
  private String schemaName;

  @Override
  public void afterMigrate(Connection connection) {
    XLOGGER.entry(connection);

    XLOGGER.info("After migrations, exporting db schema");

    int exitCode = 0;
    try {
      schemaName = Optional.ofNullable(schemaName).orElse("stockmanagement");
      
      Process proc = Runtime.getRuntime().exec("/app/export_schema.sh " + schemaName);

      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        XLOGGER.debug("STDOUT> " + line);
      }

      while ((line = errorReader.readLine()) != null) {
        XLOGGER.debug("STDERR> " + line);
      }

      exitCode = proc.waitFor();
    } catch (Exception ex) {
      XLOGGER.warn("Exporting db schema failed with message: " + ex);
    }

    XLOGGER.exit(exitCode);
  }
}
