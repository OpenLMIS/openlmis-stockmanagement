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

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;
import static org.openlmis.stockmanagement.testutils.ValidDestinationAssignmentDataBuilder.createDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceAssignmentDataBuilder.createSource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.ValidDestinationService;
import org.openlmis.stockmanagement.service.ValidSourceService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.UUID;

public class ValidSourceDestinationControllerIntegrationTest extends BaseWebTest {

  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";
  private static final String API_VALID_DESTINATIONS = "/api/validDestinations";
  private static final String API_VALID_SOURCES = "/api/validSources";
  private static final String PROGRAM_EXP = "$.programId";
  private static final String FACILITY_TYPE_EXP = "$.facilityTypeId";
  private static final String NODE_REFERENCE_ID_EXP = "$.node.referenceId";

  @MockBean
  private ValidSourceService validSourceService;

  @MockBean
  private ValidDestinationService validDestinationService;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void shouldGetValidSourcesOrDestinationsByProgramAndFacilityType()
      throws Exception {
    //given
    ValidSourceDestinationDto destinationAssignmentDto = new ValidSourceDestinationDto();
    destinationAssignmentDto.setId(randomUUID());
    destinationAssignmentDto.setName("CHW");
    destinationAssignmentDto.setIsFreeTextAllowed(true);
    ValidSourceDestinationDto sourceDestination = destinationAssignmentDto;

    UUID program = randomUUID();
    UUID facilityType = randomUUID();
    when(validSourceService.findSources(program, facilityType))
        .thenReturn(singletonList(sourceDestination));

    when(validDestinationService.findDestinations(program, facilityType))
        .thenReturn(singletonList(sourceDestination));

    //1. perform valid destinations
    performSourcesOrDestinations(program, facilityType, sourceDestination, API_VALID_DESTINATIONS);

    //2. perform valid sources
    performSourcesOrDestinations(program, facilityType, sourceDestination, API_VALID_SOURCES);
  }

  @Test
  public void shouldReturn400WhenPermissionCheckFails()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    doThrow(new ValidationMessageException(
        new Message(ERROR_PROGRAM_NOT_FOUND, programId.toString())))
        .when(permissionService)
        .canViewValidDestinations(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(get(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return201WhenAssignSourceSuccessfully() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    validSourceDestinationDto.setNode(assignment.getNode());

    when(validSourceService.assignSource(assignment))
        .thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_SOURCES)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(assignment)));

    //then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(sourceId.toString())));
  }

  @Test
  public void return201WhenAssignDestinationSuccessfully() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID destinationId = UUID.randomUUID();
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    validSourceDestinationDto.setNode(assignment.getNode());

    when(validDestinationService.assignDestination(assignment))
        .thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(assignment)));

    //then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(destinationId.toString())));
  }

  @Test
  public void shouldReturn200WhenDestinationAssignmentAlreadyExist() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID destinationId = UUID.randomUUID();
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    validSourceDestinationDto.setNode(assignment.getNode());
    when(validDestinationService.findByProgramFacilityDestination(assignment))
        .thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(assignment)));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(destinationId.toString())));
  }

  @Test
  public void shouldReturn200WhenSourceAssignmentAlreadyExist() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    validSourceDestinationDto.setNode(assignment.getNode());

    when(validSourceService.findByProgramFacilitySource(assignment))
        .thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_SOURCES)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(assignment)));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(sourceId.toString())));
  }

  @Test
  public void shouldReturn204WhenSourceAssignmentRemoved() throws Exception {
    mvc.perform(delete(API_VALID_SOURCES + "/" + randomUUID().toString())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE))
        .andExpect(status().isNoContent());
  }

  @Test
  public void shouldReturn204WhenDestinationAssignmentRemoved() throws Exception {
    mvc.perform(delete(API_VALID_DESTINATIONS + "/" + randomUUID().toString())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE))
        .andExpect(status().isNoContent());
  }

  private void performSourcesOrDestinations(
      UUID programId, UUID facilityTypeId,
      ValidSourceDestinationDto sourceDestinationDto, String uri) throws Exception {
    ResultActions resultActions = mvc.perform(get(uri)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(sourceDestinationDto.getId().toString())))
        .andExpect(jsonPath("$[0].name", is(sourceDestinationDto.getName())))
        .andExpect(jsonPath("$[0].isFreeTextAllowed", is(true)));
  }
}