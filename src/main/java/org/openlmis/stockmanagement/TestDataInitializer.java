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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openlmis.stockmanagement.util.Resource2Db;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Loads layered test data into an empty database when a data profile is active.
 *
 * <p>Each data profile is a layer stored in {@code db/<layer>/}, with one CSV file per table
 * named {@code <schema>.<table>.csv}. Every data profile implies {@code base-data}, which is
 * loaded first. A layer only holds files for the tables it adds rows to, and its rows may refer
 * only to rows of their own layer or of {@code base-data}.
 */
@Component
@Profile("(base-data | demo-data | functional-test-data | edge-case-data | performance-data)"
    + " & !test-run")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  static final String BASE_DATA = "base-data";
  static final String DEMO_DATA = "demo-data";
  static final String FUNCTIONAL_TEST_DATA = "functional-test-data";
  static final String EDGE_CASE_DATA = "edge-case-data";
  static final String PERFORMANCE_DATA = "performance-data";

  /**
   * All data layers, in load order.
   */
  static final List<String> LAYERS = Collections.unmodifiableList(Arrays.asList(
      BASE_DATA, DEMO_DATA, FUNCTIONAL_TEST_DATA, EDGE_CASE_DATA, PERFORMANCE_DATA));

  /**
   * All tables a layer may hold, in an order that satisfies the foreign keys between them.
   */
  static final List<String> TABLES = Collections.unmodifiableList(Arrays.asList(
      "stockmanagement.stock_card_line_item_reasons",
      "stockmanagement.stock_card_line_item_reason_tags",
      "stockmanagement.valid_reason_assignments",
      "stockmanagement.organizations",
      "stockmanagement.nodes",
      "stockmanagement.valid_source_assignments",
      "stockmanagement.valid_destination_assignments",
      "stockmanagement.stock_events",
      "stockmanagement.stock_event_line_items",
      "stockmanagement.stock_cards",
      "stockmanagement.stock_card_line_items",
      "stockmanagement.calculated_stocks_on_hand",
      "stockmanagement.physical_inventories",
      "stockmanagement.physical_inventory_line_items",
      "stockmanagement.physical_inventory_line_item_adjustments"));

  private final Resource2Db loader;
  private final ResourceLoader resourceLoader;
  private final Environment environment;

  @Autowired
  public TestDataInitializer(JdbcTemplate template, ResourceLoader resourceLoader,
      Environment environment) {
    this(new Resource2Db(template), resourceLoader, environment);
  }

  TestDataInitializer(Resource2Db loader, ResourceLoader resourceLoader,
      Environment environment) {
    this.loader = loader;
    this.resourceLoader = resourceLoader;
    this.environment = environment;
  }

  /**
   * Loads every table file of each active data layer.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    List<String> layers = resolveLayers(environment.getActiveProfiles());
    XLOGGER.info("Loading data layers: " + layers);

    for (String layer : layers) {
      int loaded = 0;
      for (String table : TABLES) {
        Resource resource = resourceLoader.getResource(location(layer, table));
        if (resource.exists()) {
          loader.insertToDbFromCsv(table, resource);
          loaded++;
        }
      }
      XLOGGER.info("Data layer " + layer + ": loaded " + loaded + " table file(s)");
    }

    XLOGGER.exit();
  }

  /**
   * Returns the data layers to load for the given active profiles, in load order. Any data
   * profile implies {@code base-data}.
   *
   * @throws IllegalStateException when both demo-data and functional-test-data are active
   */
  static List<String> resolveLayers(String... activeProfiles) {
    List<String> active = Arrays.asList(activeProfiles);

    if (active.contains(DEMO_DATA) && active.contains(FUNCTIONAL_TEST_DATA)) {
      throw new IllegalStateException("The " + DEMO_DATA + " and " + FUNCTIONAL_TEST_DATA
          + " profiles cannot be active together; choose one of them");
    }

    List<String> layers = new ArrayList<>();
    for (String layer : LAYERS) {
      if (BASE_DATA.equals(layer) || active.contains(layer)) {
        layers.add(layer);
      }
    }
    return layers;
  }

  static String location(String layer, String table) {
    return "classpath:db/" + layer + "/" + table + ".csv";
  }
}
