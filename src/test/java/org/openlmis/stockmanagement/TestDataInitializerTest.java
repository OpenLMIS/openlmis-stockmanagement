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

import static org.mockito.Mockito.verify;
import static org.openlmis.stockmanagement.TestDataInitializer.JASPER_TEMPLATES_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.NODES_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.ORGANIZATIONS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.PHYSICAL_INVENTORIES_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.PHYSICAL_INVENTORY_LINE_ITEMS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.STOCK_CARDS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.STOCK_CARD_LINE_ITEMS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.STOCK_CARD_LINE_ITEM_REASONS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.STOCK_CARD_LINE_ITEM_REASON_TAGS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.STOCK_EVENTS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.STOCK_EVENT_LINE_ITEMS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.VALID_DESTINATION_ASSIGNMENTS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.VALID_REASON_ASSIGNMENTS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.VALID_SOURCE_ASSIGNMENTS_TABLE;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.util.Resource2Db;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.class)
public class TestDataInitializerTest {

  @Mock
  private Resource jasperTemplatesResource;

  @Mock
  private Resource nodesResource;

  @Mock
  private Resource organizationsResource;

  @Mock
  private Resource physicalInventoriesResource;

  @Mock
  private Resource physicalInventoryLineItemsResource;

  @Mock
  private Resource stockCardLineItemReasonTagsResource;

  @Mock
  private Resource stockCardLineItemReasonsResource;

  @Mock
  private Resource stockCardLineItemsResource;

  @Mock
  private Resource stockCardsResource;

  @Mock
  private Resource stockEventLineItemsResource;

  @Mock
  private Resource stockEventsResource;

  @Mock
  private Resource validSourceAssignmentsResource;

  @Mock
  private Resource validReasonAssignmentsResource;

  @Mock
  private Resource validDestinationAssignmentsResource;

  @Mock
  private Resource2Db loader;

  @InjectMocks
  private TestDataInitializer initializer = new TestDataInitializer(loader);

  @Test
  public void shouldLoadData() throws IOException {
    initializer.run();

    verify(loader).insertToDbFromCsv(STOCK_CARD_LINE_ITEM_REASONS_TABLE, stockCardLineItemReasonsResource);
    verify(loader).insertToDbFromCsv(
        STOCK_CARD_LINE_ITEM_REASON_TAGS_TABLE, stockCardLineItemReasonTagsResource);
    verify(loader).insertToDbFromCsv(
        VALID_REASON_ASSIGNMENTS_TABLE, validReasonAssignmentsResource);
    verify(loader).insertToDbFromCsv(ORGANIZATIONS_TABLE, organizationsResource);
    verify(loader).insertToDbFromCsv(NODES_TABLE, nodesResource);
    verify(loader).insertToDbFromCsv(
        VALID_SOURCE_ASSIGNMENTS_TABLE, validSourceAssignmentsResource);
    verify(loader).insertToDbFromCsv(
        VALID_DESTINATION_ASSIGNMENTS_TABLE, validDestinationAssignmentsResource);
    verify(loader).insertToDbFromCsv(STOCK_EVENTS_TABLE, stockEventsResource);
    verify(loader).insertToDbFromCsv(STOCK_EVENT_LINE_ITEMS_TABLE, stockEventLineItemsResource);
    verify(loader).insertToDbFromCsv(STOCK_CARDS_TABLE, stockCardsResource);
    verify(loader).insertToDbFromCsv(STOCK_CARD_LINE_ITEMS_TABLE, stockCardLineItemsResource);
    verify(loader).insertToDbFromCsv(PHYSICAL_INVENTORIES_TABLE, physicalInventoriesResource);
    verify(loader).insertToDbFromCsv(
        PHYSICAL_INVENTORY_LINE_ITEMS_TABLE, physicalInventoryLineItemsResource);
    verify(loader).insertToDbFromCsv(JASPER_TEMPLATES_TABLE, jasperTemplatesResource);
  }
}
