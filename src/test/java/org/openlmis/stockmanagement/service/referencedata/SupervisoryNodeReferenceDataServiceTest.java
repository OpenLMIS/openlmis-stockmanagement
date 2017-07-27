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

package org.openlmis.stockmanagement.service.referencedata;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.testutils.DummyPage;
import java.util.Collections;
import java.util.UUID;

public class SupervisoryNodeReferenceDataServiceTest {

  private UUID facility = UUID.randomUUID();
  private UUID program = UUID.randomUUID();

  @Test
  public void shouldReturnNullIfEmptyPage() {
    SupervisoryNodeReferenceDataService spy = spy(new SupervisoryNodeReferenceDataService());
    doReturn(new DummyPage<SupervisoryNodeDto>(Collections.emptyList()))
        .when(spy)
        .getPage("search",
            Collections.emptyMap(),
            ImmutableMap.of("programId", program, "facilityId", facility));

    Assert.assertNull(spy.findSupervisoryNode(program, facility));
  }
}