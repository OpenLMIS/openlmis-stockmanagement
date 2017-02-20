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

package org.openlmis.stockmanagement.util;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

/**
 * Class containing version information.
 */
public class Version {

  public static final String VERSION = "version.properties";

  @Getter
  private String service = "service";

  @Getter
  private String build = "${build}";

  @Getter
  private String branch = "${branch}";

  @Getter
  private String timeStamp = "${time}";

  @Getter
  private String version = "version";

  private static final Logger LOGGER = LoggerFactory.getLogger(Version.class);

  /**
   * Class constructor used to fill Version with data from version file.
   */
  public Version() {

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(VERSION)) {
      if (inputStream != null) {
        Properties properties = new Properties();
        properties.load(inputStream);
        service = properties.getProperty("Service");
        version = properties.getProperty("Version");
        build = properties.getProperty("Build");
        branch = properties.getProperty("Branch");
        timeStamp = properties.getProperty("Timestamp", Instant.now().toString());
      }
    } catch (IOException ex) {
      LOGGER.error("Error loading version properties file");
    }
  }
}
