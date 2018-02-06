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

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.ValidReasonAssignmentService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class ValidReasonAssignmentControllerTest extends BaseWebTest {
  private static final String VALID_REASON_API = "/api/validReasons";
  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";
  private static final String REASON_TYPE = "reasonType";

  @MockBean
  private ValidReasonAssignmentRepository reasonAssignmentRepository;

  @MockBean
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private StockCardLineItemReasonRepository reasonRepository;

  @MockBean
  private ValidReasonAssignmentService validReasonAssignmentService;

  @Before
  public void setUp() {
    when(reasonAssignmentRepository.save(any(ValidReasonAssignment.class)))
        .thenAnswer(new SaveAnswer<ValidReasonAssignment>());
  }

  @Test
  public void getValidReasonAssignmentByProgramAndFacilityType() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();

    when(validReasonAssignmentService.search(programId, facilityTypeId, null)).thenReturn(
        Collections.singletonList(new ValidReasonAssignment()));

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void getValidReasonAssignmentByProgramAndFacilityTypeAndReasonType() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();

    ValidReasonAssignment firstAssignment =
        mockedValidReasonAssignment(UUID.randomUUID(), ReasonType.DEBIT);
    ValidReasonAssignment secondAssignment =
        mockedValidReasonAssignment(UUID.randomUUID(), ReasonType.CREDIT);

    when(validReasonAssignmentService.search(programId, facilityTypeId,
        Arrays.asList(ReasonType.CREDIT, ReasonType.DEBIT))).thenReturn(
            Arrays.asList(firstAssignment, secondAssignment));

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString())
            .param(REASON_TYPE, ReasonType.CREDIT.toString())
            .param(REASON_TYPE, ReasonType.DEBIT.toString()));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void shouldAssignReasonToProgramFacilityTypeAndSetAsShownByDefault() throws Exception {
    //given
    UUID reasonId = UUID.randomUUID();
    ValidReasonAssignment assignment = mockedValidReasonAssignment(reasonId, ReasonType.DEBIT);

    //when
    ResultActions resultActions = mvc.perform(
        post(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(assignment)));

    //then
    ArgumentCaptor<ValidReasonAssignment> assignmentCaptor = forClass(ValidReasonAssignment.class);

    resultActions
        .andDo(print())
        .andExpect(status().isCreated());
    verify(reasonAssignmentRepository, times(1)).save(assignmentCaptor.capture());
    assertThat(assignmentCaptor.getValue().getReason().getId(), is(reasonId));
    assertThat(assignmentCaptor.getValue().getHidden(), is(false));
    verify(programFacilityTypeExistenceService, times(1)).checkProgramAndFacilityTypeExist(
        assignment.getProgramId(), assignment.getFacilityTypeId());
    verify(permissionService, times(1)).canManageReasons();
  }

  @Test
  public void shouldSetValidReasonAsShownWhenHiddenIsFalse() throws Exception {
    //given1
    ValidReasonAssignment assignment = mockedValidReasonAssignment(UUID.randomUUID(),
        ReasonType.DEBIT);
    assignment.setHidden(false);

    //when
    ResultActions resultActions = mvc.perform(
        post(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(assignment)));

    //then
    ArgumentCaptor<ValidReasonAssignment> assignmentCaptor = forClass(ValidReasonAssignment.class);

    resultActions
        .andDo(print())
        .andExpect(status().isCreated());
    verify(reasonAssignmentRepository, times(1)).save(assignmentCaptor.capture());
    assertThat(assignmentCaptor.getValue().getHidden(), is(false));
  }

  @Test
  public void shouldSetValidReasonAsHiddenWhenHiddenIsTrue() throws Exception {
    //given1
    ValidReasonAssignment assignment = mockedValidReasonAssignment(UUID.randomUUID(),
        ReasonType.DEBIT);
    assignment.setHidden(true);

    //when
    ResultActions resultActions = mvc.perform(
        post(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(assignment)));

    //then
    ArgumentCaptor<ValidReasonAssignment> assignmentCaptor = forClass(ValidReasonAssignment.class);

    resultActions
        .andDo(print())
        .andExpect(status().isCreated());
    verify(reasonAssignmentRepository, times(1)).save(assignmentCaptor.capture());
    assertThat(assignmentCaptor.getValue().getHidden(), is(true));
  }

  @Test
  public void shouldIgnoreAssignmentIdWhenRequestBodySpecifiedIt() throws Exception {
    //given
    ValidReasonAssignment assignment = mockedValidReasonAssignment(UUID.randomUUID(),
        ReasonType.DEBIT);
    assignment.setId(UUID.randomUUID());

    //when
    mvc.perform(post(VALID_REASON_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(assignment)));

    //then
    ArgumentCaptor<ValidReasonAssignment> assignmentCaptor = forClass(ValidReasonAssignment.class);
    verify(reasonAssignmentRepository, times(1)).save(assignmentCaptor.capture());

    assertNotEquals(assignment.getId(), assignmentCaptor.getValue().getId());
  }

  @Test
  public void shouldNotAssignSameReasonTwice() throws Exception {
    //given
    UUID reasonId = UUID.randomUUID();
    StockCardLineItemReason reason = new StockCardLineItemReason();
    reason.setId(reasonId);

    ValidReasonAssignment assignment = new ValidReasonAssignment();
    assignment.setReason(reason);
    assignment.setProgramId(UUID.randomUUID());
    assignment.setFacilityTypeId(UUID.randomUUID());

    when(reasonRepository.exists(reasonId)).thenReturn(true);
    when(reasonAssignmentRepository.findByProgramIdAndFacilityTypeIdAndReasonId(
        assignment.getProgramId(), assignment.getFacilityTypeId(), reasonId))
        .thenReturn(new ValidReasonAssignment());

    //when
    ResultActions resultActions = mvc.perform(
        post(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(assignment)));

    //then
    resultActions.andExpect(status().isOk());
    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void shouldReturn400IfReasonIdIsNull() throws Exception {
    //when
    ResultActions resultActions = mvc.perform(
        post(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(new ValidReasonAssignment())));

    //then
    resultActions.andExpect(status().isBadRequest());
    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void shouldReturn400IfReasonNotExist() throws Exception {
    //given
    UUID reasonId = UUID.randomUUID();
    StockCardLineItemReason reason = new StockCardLineItemReason();
    reason.setId(reasonId);

    when(reasonRepository.findOne(reasonId)).thenReturn(null);
    ValidReasonAssignment assignment = new ValidReasonAssignment();
    assignment.setReason(reason);

    //when
    ResultActions resultActions = mvc.perform(
        post(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(assignment)));

    //then
    resultActions.andExpect(status().isBadRequest());
    verify(reasonAssignmentRepository, never()).save(any(ValidReasonAssignment.class));
  }

  @Test
  public void return400WhenPermissionCheckFails() throws Exception {
    //given
    //not exist in demo data
    UUID programId = randomUUID();
    UUID facilityTypeId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");

    doThrow(new ValidationMessageException("errorKey")).when(permissionService)
        .canViewValidReasons(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return204WhenRemoveReasonSuccess() throws Exception {
    UUID assignmentId = randomUUID();
    when(reasonAssignmentRepository.exists(assignmentId)).thenReturn(true);

    ResultActions resultActions = mvc.perform(
        delete(VALID_REASON_API + "/" + assignmentId.toString())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isNoContent());
    verify(permissionService, times(1)).canManageReasons();
    verify(reasonAssignmentRepository, times(1)).delete(assignmentId);
  }

  @Test
  public void return400WhenReasonIsNotFound() throws Exception {
    UUID assignmentId = randomUUID();
    when(reasonAssignmentRepository.exists(assignmentId)).thenReturn(false);

    ResultActions resultActions = mvc.perform(
        delete(VALID_REASON_API + "/" + assignmentId.toString())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    resultActions.andExpect(status().isBadRequest());
  }

  private ValidReasonAssignment mockedValidReasonAssignment(UUID reasonId, ReasonType reasonType) {
    StockCardLineItemReason reason = new StockCardLineItemReason();
    reason.setId(reasonId);
    reason.setReasonType(reasonType);

    ValidReasonAssignment assignment = new ValidReasonAssignment();
    assignment.setReason(reason);
    assignment.setProgramId(reasonId);
    assignment.setFacilityTypeId(reasonId);

    when(reasonRepository.exists(assignment.getReason().getId())).thenReturn(true);
    return assignment;
  }
}