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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import java.util.UUID;

public class OrderableFulfillReferenceDataServiceTest {

  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String SEARCH_URI = "search";
  private UUID facility = UUID.randomUUID();
  private UUID program = UUID.randomUUID();
  private SupervisoryNodeDto supervisoryNode = mock(SupervisoryNodeDto.class);
  private SupervisoryNodeReferenceDataService spy;

  @Before
  public void setUp() {
    spy = spy(new SupervisoryNodeReferenceDataService());
  }

  @Test
  public void shouldReturnNullIfEmptyPage() {
    doReturn(new DummyPage<SupervisoryNodeDto>(Collections.emptyList()))
        .when(spy)
        .getPage(SEARCH_URI,
            Collections.emptyMap(),
            ImmutableMap.of(PROGRAM_ID, program, FACILITY_ID, facility));

    assertNull(spy.findSupervisoryNode(program, facility));
  }

  @Test
  public void shouldReturnFirstElementIfMoreThanOneFound() {
    SupervisoryNodeReferenceDataService spy = spy(new SupervisoryNodeReferenceDataService());
    SupervisoryNodeDto secondNode = new SupervisoryNodeDto();
    List<SupervisoryNodeDto> found = Arrays.asList(supervisoryNode, secondNode);
    doReturn(new DummyPage<>(found))
        .when(spy)
        .getPage(SEARCH_URI,
            Collections.emptyMap(),
            ImmutableMap.of(PROGRAM_ID, program, FACILITY_ID, facility));

    SupervisoryNodeDto foundNode = spy.findSupervisoryNode(program, facility);

    assertEquals(supervisoryNode, foundNode);
    assertNotEquals(secondNode, foundNode);
  }

  @Test
  public void shouldReturnFirstElementFoundElementIfOneFound() {
    SupervisoryNodeReferenceDataService spy = spy(new SupervisoryNodeReferenceDataService());
    doReturn(new DummyPage<>(Collections.singletonList(supervisoryNode)))
        .when(spy)
        .getPage(SEARCH_URI,
            Collections.emptyMap(),
            ImmutableMap.of(PROGRAM_ID, program, FACILITY_ID, facility));

    SupervisoryNodeDto foundNode = spy.findSupervisoryNode(program, facility);

    assertEquals(supervisoryNode, foundNode);
  }
}
