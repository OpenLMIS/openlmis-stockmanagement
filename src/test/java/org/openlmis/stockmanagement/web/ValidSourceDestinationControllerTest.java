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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.ValidSourceDestinationService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

public class ValidSourceDestinationControllerTest extends BaseWebTest {

  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";
  private static final String API_VALID_DESTINATIONS = "/api/validDestinations";
  private static final String API_VALID_SOURCES = "/api/validSources";
  private static final String PROGRAM_EXP = "$.programId";
  private static final String FACILITY_TYPE_EXP = "$.facilityTypeId";
  private static final String NODE_REFERENCE_ID_EXP = "$.node.referenceId";

  @MockBean
  private ValidSourceDestinationService validSourceDestinationService;

  @Test
  public void should_get_valid_sources_or_destinations_by_program_and_facilityType()
      throws Exception {
    //given
    ValidSourceDestinationDto sourceDestination = createValidSourceDestinationDto();

    UUID program = randomUUID();
    UUID facilityType = randomUUID();
    when(validSourceDestinationService.findSources(program, facilityType))
        .thenReturn(singletonList(sourceDestination));

    when(validSourceDestinationService.findDestinations(program, facilityType))
        .thenReturn(singletonList(sourceDestination));

    //1. perform valid destinations
    performSourcesOrDestinations(program, facilityType, sourceDestination, API_VALID_DESTINATIONS);

    //2. perform valid sources
    performSourcesOrDestinations(program, facilityType, sourceDestination, API_VALID_SOURCES);
  }

  @Test
  public void should_return_400_when_program_and_facilityType_not_found_in_ref_data()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    doThrow(new ValidationMessageException(
        new Message(ERROR_PROGRAM_NOT_FOUND, programId.toString())))
        .when(validSourceDestinationService)
        .findDestinations(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void return_201_when_assign_source_successfully() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    Node node = new Node();
    node.setReferenceId(sourceId);
    validSourceDestinationDto.setNode(node);
    when(validSourceDestinationService.assignSource(programId, facilityTypeId, sourceId))
        .thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_SOURCES)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(sourceId.toString())));

    //then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(sourceId.toString())));
  }

  @Test
  public void return_201_when_assign_destination_successfully() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID destinationId = UUID.randomUUID();

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    Node node = new Node();
    node.setReferenceId(destinationId);
    validSourceDestinationDto.setNode(node);
    when(validSourceDestinationService.assignDestination(programId, facilityTypeId, destinationId))
        .thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(destinationId.toString())));

    //then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(destinationId.toString())));
  }

  @Test
  public void should_return_200_when_destination_assignment_already_exist() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    UUID destiantionId = UUID.randomUUID();

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    Node node = new Node();
    node.setReferenceId(destiantionId);
    validSourceDestinationDto.setNode(node);
    when(validSourceDestinationService.findByProgramFacilityDestination(
        programId, facilityTypeId, destiantionId)).thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(destiantionId.toString())));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(destiantionId.toString())));
  }

  @Test
  public void should_return_200_when_source_assignment_already_exist() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    ValidSourceDestinationDto validSourceDestinationDto = new ValidSourceDestinationDto();
    validSourceDestinationDto.setProgramId(programId);
    validSourceDestinationDto.setFacilityTypeId(facilityTypeId);
    Node node = new Node();
    node.setReferenceId(sourceId);
    validSourceDestinationDto.setNode(node);
    when(validSourceDestinationService.findByProgramFacilitySource(
        programId, facilityTypeId, sourceId)).thenReturn(validSourceDestinationDto);

    //when
    ResultActions resultActions = mvc.perform(post(API_VALID_SOURCES)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param(PROGRAM, programId.toString())
        .param(FACILITY_TYPE, facilityTypeId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(sourceId.toString())));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath(PROGRAM_EXP, is(programId.toString())))
        .andExpect(jsonPath(FACILITY_TYPE_EXP, is(facilityTypeId.toString())))
        .andExpect(jsonPath(NODE_REFERENCE_ID_EXP, is(sourceId.toString())));
  }

  @Test
  public void should_return_204_when_source_assignment_removed() throws Exception {
    mvc.perform(delete(API_VALID_SOURCES + randomUUID().toString())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE))
        .andExpect(status().isNoContent());
  }

  @Test
  public void should_return_204_when_destination_assignment_removed() throws Exception {
    mvc.perform(delete(API_VALID_DESTINATIONS + randomUUID().toString())
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE))
        .andExpect(status().isNoContent());
  }

  private void performSourcesOrDestinations(
      UUID programId, UUID facilityTypeId,
      ValidSourceDestinationDto sourceDestinationDto, String uri) throws Exception {
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(uri)
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

  private ValidSourceDestinationDto createValidSourceDestinationDto() {
    ValidSourceDestinationDto destinationAssignmentDto = new ValidSourceDestinationDto();
    destinationAssignmentDto.setId(randomUUID());
    destinationAssignmentDto.setName("CHW");
    destinationAssignmentDto.setIsFreeTextAllowed(true);
    return destinationAssignmentDto;
  }
}