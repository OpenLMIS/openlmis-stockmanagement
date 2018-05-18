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

import java.io.IOException;
import org.openlmis.stockmanagement.util.Resource2Db;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("performance-data")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  private static final String PERF_DATA_PATH = "classpath:db/performance-data/";
  private static final String FILE_EXTENSION = ".csv";

  // table names
  private static final String NODES = "nodes";
  private static final String VALID_SOURCE_ASSIGNMENTS = "valid_source_assignments";
  private static final String VALID_DESTINATION_ASSIGNMENTS = "valid_destination_assignments";

  // resource path
  private static final String NODES_FILE = PERF_DATA_PATH + NODES + FILE_EXTENSION;
  private static final String VALID_SOURCE_ASSIGNMENTS_FILE =
      PERF_DATA_PATH + VALID_SOURCE_ASSIGNMENTS + FILE_EXTENSION;
  private static final String VALID_DESTINATION_ASSIGNMENTS_FILE =
      PERF_DATA_PATH + VALID_DESTINATION_ASSIGNMENTS + FILE_EXTENSION;

  // database path
  private static final String DB_SCHEMA = "stockmanagement.";
  static final String NODES_TABLE = DB_SCHEMA + NODES;
  static final String VALID_SOURCE_ASSIGNMENTS_TABLE = DB_SCHEMA + VALID_SOURCE_ASSIGNMENTS;
  static final String VALID_DESTINATION_ASSIGNMENTS_TABLE =
      DB_SCHEMA + VALID_DESTINATION_ASSIGNMENTS;


  @Value(value = NODES_FILE)
  private Resource nodesResource;

  @Value(value = VALID_SOURCE_ASSIGNMENTS_FILE)
  private Resource validSourceAssignmentsResource;

  @Value(value = VALID_DESTINATION_ASSIGNMENTS_FILE)
  private Resource validDestinationAssignmentsResource;

  private Resource2Db loader;

  @Autowired
  public TestDataInitializer(JdbcTemplate template) {
    this(new Resource2Db(template));
  }

  TestDataInitializer(Resource2Db loader) {
    this.loader = loader;
  }

  /**
   * Initializes test data.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    loader.insertToDbFromCsv(NODES_TABLE, nodesResource);

    loader.insertToDbFromCsv(VALID_SOURCE_ASSIGNMENTS_TABLE, validSourceAssignmentsResource);
    loader.insertToDbFromCsv(
        VALID_DESTINATION_ASSIGNMENTS_TABLE, validDestinationAssignmentsResource);

    XLOGGER.exit();
  }

}
