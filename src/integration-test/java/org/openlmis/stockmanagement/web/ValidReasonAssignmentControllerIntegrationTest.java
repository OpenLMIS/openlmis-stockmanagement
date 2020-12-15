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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.dto.ValidReasonAssignmentDto;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.ValidReasonAssignmentDataBuilder;
import org.openlmis.stockmanagement.web.BaseWebTest.SaveAnswer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class ValidReasonAssignmentControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String VALID_REASON_API = "/api/validReasons";
  private static final String ID_URL = VALID_REASON_API + "/{id}";
  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";
  private static final String REASON_TYPE = "reasonType";
  private static final String REASON = "reason";

  @MockBean
  private ValidReasonAssignmentRepository reasonAssignmentRepository;

  @MockBean
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  private ValidReasonAssignment reasonAssignment;
  private UUID programId = UUID.randomUUID();
  private UUID facilityTypeId = UUID.randomUUID();
  private UUID reasonId = UUID.randomUUID();

  @Before
  public void setUp() {
    reasonAssignment = new ValidReasonAssignmentDataBuilder().build();

    when(reasonAssignmentRepository.save(any(ValidReasonAssignment.class)))
        .thenAnswer(new SaveAnswer<ValidReasonAssignment>());
  }

  @Test
  public void getValidReasonAssignments() {
    when(reasonAssignmentRepository.search(null, null, null, null)).thenReturn(
        Collections.singletonList(reasonAssignment));

    List<LinkedHashMap<String, String>> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(VALID_REASON_API)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(List.class);

    verifyZeroInteractions(permissionService);

    assertThat(response.get(0).get("id"), is(reasonAssignment.getId().toString()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());
  }

  @Test
  public void getValidReasonAssignmentsByAllParameters() {
    when(reasonAssignmentRepository.search(programId, facilityTypeId,
        Sets.newHashSet(ReasonType.CREDIT, ReasonType.DEBIT), reasonId)).thenReturn(
        Collections.singletonList(reasonAssignment));

    List<LinkedHashMap<String, String>> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM, programId)
        .queryParam(FACILITY_TYPE, facilityTypeId)
        .queryParam(REASON_TYPE, ReasonType.CREDIT)
        .queryParam(REASON_TYPE, ReasonType.DEBIT)
        .queryParam(REASON, reasonId)
        .when()
        .get(VALID_REASON_API)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(List.class);

    //then
    verifyZeroInteractions(permissionService);

    assertThat(response.get(0).get("id"), is(reasonAssignment.getId().toString()));
    assertThat(RAML_ASSERT_MESSAGE,
        restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getValidReasonAssignmentByReason() {
    when(reasonAssignmentRepository.search(null, null, null, reasonId)).thenReturn(
        Collections.singletonList(reasonAssignment));

    List<LinkedHashMap<String, String>> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(REASON, reasonId)
        .when()
        .get(VALID_REASON_API)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(List.class);

    verifyZeroInteractions(permissionService);

    assertThat(response.get(0).get("id"), is(reasonAssignment.getId().toString()));
    assertThat(RAML_ASSERT_MESSAGE,
        restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAssignReasonToProgramFacilityType() {
    ValidReasonAssignmentDto assignment = mockedValidReasonAssignment(false);

    ValidReasonAssignmentDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(assignment)
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(201)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    assertThat(response.getReason().getId(), is(assignment.getReason().getId()));
    assertThat(response.getHidden(), is(false));
    verify(programFacilityTypeExistenceService, times(1)).checkProgramAndFacilityTypeExist(
        assignment.getProgramId(), assignment.getFacilityTypeId());
    verify(permissionService, times(1)).canManageReasons();
    assertThat(RAML_ASSERT_MESSAGE,
        restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSetValidReasonAsShownWhenHiddenIsFalse() {
    ValidReasonAssignmentDto assignment = mockedValidReasonAssignment(false);

    ValidReasonAssignmentDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(assignment)
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(201)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    assertThat(response.getHidden(), is(false));
    assertThat(RAML_ASSERT_MESSAGE,
        restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSetValidReasonAsHiddenWhenHiddenIsTrue() {
    ValidReasonAssignmentDto assignment = mockedValidReasonAssignment(true);

    ValidReasonAssignmentDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(assignment)
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(201)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    assertThat(response.getHidden(), is(true));
    assertThat(RAML_ASSERT_MESSAGE,
        restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldIgnoreAssignmentIdWhenRequestBodySpecifiedIt() {
    ValidReasonAssignmentDto assignment = mockedValidReasonAssignment(false);

    ValidReasonAssignmentDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(assignment)
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(201)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    assertNotEquals(response.getId(), is(assignment.getId()));
    assertThat(RAML_ASSERT_MESSAGE,
        restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAssignSameReasonTwice() {
    StockCardLineItemReason reason = new StockCardLineItemReason();
    reason.setId(reasonId);

    ValidReasonAssignmentDto assignment = new ValidReasonAssignmentDto();
    assignment.setReason(reason);
    assignment.setProgramId(UUID.randomUUID());
    assignment.setFacilityTypeId(UUID.randomUUID());

    when(stockCardLineItemReasonRepository.existsById(reasonId)).thenReturn(true);
    when(reasonAssignmentRepository.findByProgramIdAndFacilityTypeIdAndReasonId(
        assignment.getProgramId(), assignment.getFacilityTypeId(), reasonId))
        .thenReturn(new ValidReasonAssignment());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(assignment)
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(400)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void shouldReturn400IfReasonIdIsNull() throws Exception {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(new ValidReasonAssignment())
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(400)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void shouldReturn400IfReasonNotExist() {
    when(stockCardLineItemReasonRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(reasonAssignment)
        .when()
        .post(VALID_REASON_API)
        .then()
        .statusCode(400)
        .extract()
        .as(ValidReasonAssignmentDto.class);

    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void return204WhenRemoveReasonSuccess() {
    when(reasonAssignmentRepository.existsById(any(UUID.class))).thenReturn(true);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonAssignment.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    verify(permissionService, times(1)).canManageReasons();
    verify(reasonAssignmentRepository, times(1)).deleteById(reasonAssignment.getId());
  }

  @Test
  public void return400WhenReasonIsNotFound() {
    when(reasonAssignmentRepository.existsById(any(UUID.class))).thenReturn(false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonAssignment.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(400);
  }

  private ValidReasonAssignmentDto mockedValidReasonAssignment(boolean isHidden) {
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().build();

    ValidReasonAssignmentDto assignment = new ValidReasonAssignmentDto();
    assignment.setReason(reason);
    assignment.setProgramId(reasonId);
    assignment.setFacilityTypeId(reasonId);
    assignment.setHidden(isHidden);

    when(stockCardLineItemReasonRepository.existsById(assignment.getReason().getId()))
        .thenReturn(true);
    return assignment;
  }
}