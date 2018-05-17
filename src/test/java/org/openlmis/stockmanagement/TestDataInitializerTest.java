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
import static org.openlmis.stockmanagement.TestDataInitializer.NODES_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.VALID_DESTINATION_ASSIGNMENTS_TABLE;
import static org.openlmis.stockmanagement.TestDataInitializer.VALID_SOURCE_ASSIGNMENTS_TABLE;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.util.Resource2Db;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TestDataInitializerTest {

  @Mock
  private Resource nodesResource;

  @Mock
  private Resource validSourceAssignmentsResource;

  @Mock
  private Resource validDestinationAssignmentsResource;

  @InjectMocks
  private TestDataInitializer initializer;

  @Mock
  private Resource2Db loader;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(initializer, "loader", loader);
  }

  @Test
  public void shouldLoadData() throws IOException {
    initializer.run();

    verify(loader).insertToDbFromCsv(NODES_TABLE, nodesResource);
    verify(loader).insertToDbFromCsv(
        VALID_SOURCE_ASSIGNMENTS_TABLE, validSourceAssignmentsResource);
    verify(loader).insertToDbFromCsv(
        VALID_DESTINATION_ASSIGNMENTS_TABLE, validDestinationAssignmentsResource);
  }
}
