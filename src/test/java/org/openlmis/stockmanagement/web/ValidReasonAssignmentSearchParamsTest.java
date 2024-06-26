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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openlmis.stockmanagement.web.ValidReasonAssignmentSearchParams.FACILITY_TYPE;
import static org.openlmis.stockmanagement.web.ValidReasonAssignmentSearchParams.PROGRAM;
import static org.openlmis.stockmanagement.web.ValidReasonAssignmentSearchParams.REASON;
import static org.openlmis.stockmanagement.web.ValidReasonAssignmentSearchParams.REASON_TYPE;

import com.google.common.collect.Sets;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.springframework.util.LinkedMultiValueMap;

public class ValidReasonAssignmentSearchParamsTest {

  private static final UUID VALUE = UUID.randomUUID();
  private static final String DEBIT = "DEBIT";
  private static final String CREDIT = "CREDIT";

  @Test
  public void shouldGetProgramIdValueFromParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    final UUID typeId = UUID.randomUUID();
    queryMap.add(PROGRAM, VALUE.toString());
    queryMap.add(FACILITY_TYPE, typeId.toString());
    ValidReasonAssignmentSearchParams params = new ValidReasonAssignmentSearchParams(queryMap);

    assertTrue(params.getPrograms().contains(VALUE));
    assertFalse(params.getPrograms().contains(typeId));
  }

  @Test
  public void shouldAssignNullIfProgramIdIsAbsentInParameters() {
    ValidReasonAssignmentSearchParams params =
        new ValidReasonAssignmentSearchParams(new LinkedMultiValueMap<>());

    assertNull(params.getPrograms());
  }

  @Test
  public void shouldGetFacilityTypeValueFromParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(FACILITY_TYPE, VALUE.toString());
    ValidReasonAssignmentSearchParams params = new ValidReasonAssignmentSearchParams(queryMap);

    assertEquals(VALUE, params.getFacilityType());
  }

  @Test
  public void shouldAssignNullIfNameIsAbsentInParameters() {
    ValidReasonAssignmentSearchParams params =
        new ValidReasonAssignmentSearchParams(new LinkedMultiValueMap<>());

    assertNull(params.getFacilityType());
  }

  @Test
  public void shouldGetReasonIdValueFromParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(REASON, VALUE.toString());
    ValidReasonAssignmentSearchParams params = new ValidReasonAssignmentSearchParams(queryMap);

    assertEquals(VALUE, params.getReason());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfThereIsUnknownReasonIdParameterInParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(REASON_TYPE, "some-value");
    new ValidReasonAssignmentSearchParams(queryMap);
  }

  @Test
  public void shouldAssignNullIfReasonIdIsAbsentInParameters() {
    ValidReasonAssignmentSearchParams params =
        new ValidReasonAssignmentSearchParams(new LinkedMultiValueMap<>());

    assertNull(params.getReason());
  }

  @Test
  public void shouldGetReasonTypesFromParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(REASON_TYPE, DEBIT);
    queryMap.add(REASON_TYPE, CREDIT);
    ValidReasonAssignmentSearchParams params = new ValidReasonAssignmentSearchParams(queryMap);

    assertEquals(Sets.newHashSet(ReasonType.fromString(CREDIT), ReasonType.fromString(DEBIT)),
        params.getReasonType());
  }

  @Test
  public void shouldAssignEmptySetIfIdsAreAbsentInParameters() {
    ValidReasonAssignmentSearchParams params =
        new ValidReasonAssignmentSearchParams(new LinkedMultiValueMap<>());

    assertNull(params.getReasonType());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfThereIsUnknownParameterInParameters() {
    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("someParameter", "some-value");
    new ValidReasonAssignmentSearchParams(queryMap);
  }

}
