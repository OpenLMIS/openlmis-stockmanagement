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

package org.openlmis.stockmanagement.web;

import org.junit.Test;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.springframework.util.LinkedMultiValueMap;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openlmis.stockmanagement.web.ValidSourceDestinationSearchParams.FACILITY_ID;
import static org.openlmis.stockmanagement.web.ValidSourceDestinationSearchParams.PROGRAM_ID;

public class ValidSourceDestinationSearchParamsTest {

  private static final UUID PROGRAM_ID_VALUE = UUID.randomUUID();
  private static final UUID FACILITY_ID_VALUE = UUID.randomUUID();

  @Test
  public void shouldGetProgramIdValueFromParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM_ID, PROGRAM_ID_VALUE.toString());
    queryMap.add(FACILITY_ID, FACILITY_ID_VALUE.toString());
    ValidSourceDestinationSearchParams params = new ValidSourceDestinationSearchParams(queryMap);

    assertEquals(PROGRAM_ID_VALUE, params.getProgramId());
  }

  @Test
  public void shouldAssignNullIfProgramIdIsAbsentInParameters() {
    ValidSourceDestinationSearchParams params =
            new ValidSourceDestinationSearchParams(new LinkedMultiValueMap<>());

    assertNull(params.getProgramId());
  }

  @Test
  public void shouldGetFacilityIdValueFromParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM_ID, PROGRAM_ID_VALUE.toString());
    queryMap.add(FACILITY_ID, FACILITY_ID_VALUE.toString());
    ValidSourceDestinationSearchParams params = new ValidSourceDestinationSearchParams(queryMap);

    assertEquals(FACILITY_ID_VALUE, params.getFacilityId());
  }

  @Test
  public void shouldAssignNullIfFacilityIdIsAbsentInParameters() {
    ValidSourceDestinationSearchParams params =
            new ValidSourceDestinationSearchParams(new LinkedMultiValueMap<>());

    assertNull(params.getFacilityId());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfThereIsUnknownReasonIdParameterInParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("unknownProperty", "some-value");
    new ValidSourceDestinationSearchParams(queryMap);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfOnlyProgramIdIsProvided() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM_ID, PROGRAM_ID_VALUE.toString());
    new ValidSourceDestinationSearchParams(queryMap);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfOnlyFacilityIdIsProvided() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(FACILITY_ID, FACILITY_ID_VALUE.toString());
    new ValidSourceDestinationSearchParams(queryMap);
  }

  @Test
  public void shouldNotThrowExceptionIfAnyParameterIsProvided() {
    new ValidSourceDestinationSearchParams(new LinkedMultiValueMap<>());
  }

}
