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

package org.openlmis.stockmanagement.dto.builder;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.ValidReasonAssignmentDto;
import org.openlmis.stockmanagement.service.ResourceNames;
import org.openlmis.stockmanagement.testutils.ValidReasonAssignmentDataBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ValidReasonAssignmentDtoBuilderTest {

  private ValidReasonAssignmentDtoBuilder dtoBuilder = new ValidReasonAssignmentDtoBuilder();

  private static final String SERVICE_URL = "localhost/";

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(dtoBuilder, "serviceUrl", SERVICE_URL);
  }

  @Test
  public void shouldReturnEmptyListIfNullIsPassedToBuildListOfDtos() {
    List<ValidReasonAssignmentDto> actualDtos = dtoBuilder.build((List) null);
    assertNotNull(actualDtos);
    assertTrue(actualDtos.isEmpty());
  }

  @Test
  public void shouldReturnNullIfNullIsPassedToBuildDto() {
    ValidReasonAssignmentDto dto = dtoBuilder.build((ValidReasonAssignment) null);
    assertNull(dto);
  }

  @Test
  public void shouldBuildDtoWithProperServiceUrls() {
    ValidReasonAssignment assignment = new ValidReasonAssignmentDataBuilder().build();

    ValidReasonAssignmentDto dto = dtoBuilder.build(assignment);

    assertValidReasonAssignmentDto(assignment, dto);
  }

  @Test
  public void shouldBuildListOfDtosWithProperServiceUrls() {
    ValidReasonAssignment assignment = new ValidReasonAssignmentDataBuilder().build();
    ValidReasonAssignment assignment2 = new ValidReasonAssignmentDataBuilder()
        .withFacilityType(UUID.randomUUID())
        .withProgram(UUID.randomUUID())
        .build();

    List<ValidReasonAssignmentDto> dtos =
        dtoBuilder.build(Lists.newArrayList(assignment, assignment2));

    assertEquals(2, dtos.size());

    assertValidReasonAssignmentDto(assignment, dtos.get(0));
    assertValidReasonAssignmentDto(assignment2, dtos.get(1));
  }


  private void assertValidReasonAssignmentDto(ValidReasonAssignment assignment,
                                              ValidReasonAssignmentDto dto) {
    assertEquals(assignment.getId(), dto.getId());
    assertEquals(assignment.getHidden(), dto.getHidden());
    assertEquals(SERVICE_URL + ResourceNames.SEPARATOR + ResourceNames.FACILITY_TYPES
            + ResourceNames.SEPARATOR + assignment.getFacilityTypeId(),
        dto.getFacilityType().getHref());
    assertEquals(SERVICE_URL + ResourceNames.SEPARATOR + ResourceNames.PROGRAMS
            + ResourceNames.SEPARATOR + assignment.getProgramId(),
        dto.getProgram().getHref());
    assertEquals(assignment.getProgramId(), dto.getProgramId());
    assertEquals(assignment.getFacilityTypeId(), dto.getFacilityTypeId());
  }
}
