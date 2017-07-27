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

package org.openlmis.stockmanagement.service;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import java.util.Collections;
import java.util.UUID;

public class SupervisingUsersReferenceDataServiceTest {

  private UUID supervisoryNode = UUID.randomUUID();
  private UUID right = UUID.randomUUID();
  private UUID program = UUID.randomUUID();

  @Test
  public void testFindAll() {
    SupervisingUsersReferenceDataService spy = spy(new SupervisingUsersReferenceDataService());
    doReturn(Collections.emptyList()).when(spy).findAll(anyString(), anyMap());

    spy.findAll(supervisoryNode, right, program);

    Mockito.verify(spy).findAll(supervisoryNode + "/supervisingUsers",
        ImmutableMap.of("rightId", right, "programId", program));
  }
}