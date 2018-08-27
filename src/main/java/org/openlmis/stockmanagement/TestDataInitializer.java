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
@Profile("demo-data")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  private static final String DEMO_DATA_PATH = "classpath:db/demo-data/";
  private static final String FILE_EXTENSION = ".csv";

  // table names
  private static final String JASPER_TEMPLATES = "jasper_templates";
  private static final String NODES = "nodes";
  private static final String ORGANIZATIONS = "organizations";
  private static final String PHYSICAL_INVENTORIES = "physical_inventories";
  private static final String PHYSICAL_INVENTORY_LINE_ITEMS = "physical_inventory_line_items";
  private static final String STOCK_CARD_LINE_ITEM_REASON_TAGS = "stock_card_line_item_reason_tags";
  private static final String STOCK_CARD_LINE_ITEM_REASONS = "stock_card_line_item_reasons";
  private static final String STOCK_CARD_LINE_ITEMS = "stock_card_line_items";
  private static final String STOCK_CARDS = "stock_cards";
  private static final String STOCK_EVENT_LINE_ITEMS = "stock_event_line_items";
  private static final String STOCK_EVENTS = "stock_events";
  private static final String VALID_SOURCE_ASSIGNMENTS = "valid_source_assignments";
  private static final String VALID_REASON_ASSIGNMENTS = "valid_reason_assignments";
  private static final String VALID_DESTINATION_ASSIGNMENTS = "valid_destination_assignments";

  // database path
  private static final String DB_SCHEMA = "stockmanagement.";
  static final String JASPER_TEMPLATES_TABLE = DB_SCHEMA + JASPER_TEMPLATES;
  static final String NODES_TABLE = DB_SCHEMA + NODES;
  static final String ORGANIZATIONS_TABLE = DB_SCHEMA + ORGANIZATIONS;
  static final String PHYSICAL_INVENTORIES_TABLE = DB_SCHEMA + PHYSICAL_INVENTORIES;
  static final String PHYSICAL_INVENTORY_LINE_ITEMS_TABLE = 
      DB_SCHEMA + PHYSICAL_INVENTORY_LINE_ITEMS;
  static final String STOCK_CARD_LINE_ITEM_REASON_TAGS_TABLE = 
      DB_SCHEMA + STOCK_CARD_LINE_ITEM_REASON_TAGS;
  static final String STOCK_CARD_LINE_ITEM_REASONS_TABLE = DB_SCHEMA + STOCK_CARD_LINE_ITEM_REASONS;
  static final String STOCK_CARD_LINE_ITEMS_TABLE = DB_SCHEMA + STOCK_CARD_LINE_ITEMS;
  static final String STOCK_CARDS_TABLE = DB_SCHEMA + STOCK_CARDS;
  static final String STOCK_EVENT_LINE_ITEMS_TABLE = DB_SCHEMA + STOCK_EVENT_LINE_ITEMS;
  static final String STOCK_EVENTS_TABLE = DB_SCHEMA + STOCK_EVENTS;
  static final String VALID_SOURCE_ASSIGNMENTS_TABLE = DB_SCHEMA + VALID_SOURCE_ASSIGNMENTS;
  static final String VALID_REASON_ASSIGNMENTS_TABLE = DB_SCHEMA + VALID_REASON_ASSIGNMENTS;
  static final String VALID_DESTINATION_ASSIGNMENTS_TABLE =
      DB_SCHEMA + VALID_DESTINATION_ASSIGNMENTS;


  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + JASPER_TEMPLATES + FILE_EXTENSION)
  private Resource jasperTemplatesResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + NODES + FILE_EXTENSION)
  private Resource nodesResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + ORGANIZATIONS + FILE_EXTENSION)
  private Resource organizationsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + PHYSICAL_INVENTORIES + FILE_EXTENSION)
  private Resource physicalInventoriesResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + PHYSICAL_INVENTORY_LINE_ITEMS + FILE_EXTENSION)
  private Resource physicalInventoryLineItemsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + STOCK_CARD_LINE_ITEM_REASON_TAGS + FILE_EXTENSION)
  private Resource stockCardLineItemReasonTagsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + STOCK_CARD_LINE_ITEM_REASONS + FILE_EXTENSION)
  private Resource stockCardLineItemReasonsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + STOCK_CARD_LINE_ITEMS + FILE_EXTENSION)
  private Resource stockCardLineItemsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + STOCK_CARDS + FILE_EXTENSION)
  private Resource stockCardsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + STOCK_EVENT_LINE_ITEMS + FILE_EXTENSION)
  private Resource stockEventLineItemsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + STOCK_EVENTS + FILE_EXTENSION)
  private Resource stockEventsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + VALID_SOURCE_ASSIGNMENTS + FILE_EXTENSION)
  private Resource validSourceAssignmentsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + VALID_REASON_ASSIGNMENTS + FILE_EXTENSION)
  private Resource validReasonAssignmentsResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + VALID_DESTINATION_ASSIGNMENTS + FILE_EXTENSION)
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

    loader.insertToDbFromCsv(STOCK_CARD_LINE_ITEM_REASONS_TABLE, stockCardLineItemReasonsResource);
    loader.insertToDbFromCsv(
        STOCK_CARD_LINE_ITEM_REASON_TAGS_TABLE, stockCardLineItemReasonTagsResource);
    loader.insertToDbFromCsv(VALID_REASON_ASSIGNMENTS_TABLE, validReasonAssignmentsResource);
    loader.insertToDbFromCsv(ORGANIZATIONS_TABLE, organizationsResource);
    loader.insertToDbFromCsv(NODES_TABLE, nodesResource);
    loader.insertToDbFromCsv(VALID_SOURCE_ASSIGNMENTS_TABLE, validSourceAssignmentsResource);
    loader.insertToDbFromCsv(
        VALID_DESTINATION_ASSIGNMENTS_TABLE, validDestinationAssignmentsResource);
    loader.insertToDbFromCsv(STOCK_EVENTS_TABLE, stockEventsResource);
    loader.insertToDbFromCsv(STOCK_EVENT_LINE_ITEMS_TABLE, stockEventLineItemsResource);
    loader.insertToDbFromCsv(STOCK_CARDS_TABLE, stockCardsResource);
    loader.insertToDbFromCsv(STOCK_CARD_LINE_ITEMS_TABLE, stockCardLineItemsResource);
    loader.insertToDbFromCsv(PHYSICAL_INVENTORIES_TABLE, physicalInventoriesResource);
    loader.insertToDbFromCsv(
        PHYSICAL_INVENTORY_LINE_ITEMS_TABLE, physicalInventoryLineItemsResource);
    loader.insertToDbFromCsv(JASPER_TEMPLATES_TABLE, jasperTemplatesResource);

    XLOGGER.exit();
  }

}
