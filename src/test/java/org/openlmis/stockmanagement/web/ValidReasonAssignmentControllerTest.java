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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.adjustment.ValidReasonAssignment;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.service.ProgramFacilityTypePermissionService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.UUID;

public class ValidReasonAssignmentControllerTest extends BaseWebTest {
  private static final String GET_VALID_REASON_API = "/api/validReasons";
  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";

  @MockBean
  private ValidReasonAssignmentRepository reasonAssignmentRepository;

  @MockBean
  private ProgramFacilityTypePermissionService programFacilityTypePermissionService;

  @MockBean
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Test
  public void get_valid_reason_by_program_and_facility_type() throws Exception {
    //given
    //exist in demo data
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    UUID facilityTypeId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");

    when(reasonAssignmentRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(Arrays.asList(new ValidReasonAssignment()));

    //when
    ResultActions resultActions = mvc.perform(
        get(GET_VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void return_400_when_program_not_found() throws Exception {
    //given
    //not exist in demo data
    UUID programId = randomUUID();
    UUID facilityTypeId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");

    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(GET_VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return_400_when_facility_type_not_found() throws Exception {
    //given
    //not exist in demo data
    UUID facilityTypeId = randomUUID();
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(GET_VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return_403_when_facility_type_and_home_facility_type_not_match() throws Exception {
    //given
    UUID facilityTypeId = randomUUID();
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    doThrow(new PermissionMessageException(new Message("errorKey")))
        .when(programFacilityTypePermissionService).checkProgramFacility(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(
        get(GET_VALID_REASON_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param(PROGRAM, programId.toString())
            .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isForbidden());
  }
}