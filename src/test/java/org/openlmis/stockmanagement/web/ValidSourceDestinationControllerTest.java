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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.ValidSourceDestinationService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

public class ValidSourceDestinationControllerTest extends BaseWebTest {

  @MockBean
  private ValidSourceDestinationService validSourceDestinationService;

  private static final String API_VALID_DESTINATIONS = "/api/validDestinations";
  private static final String API_VALID_SOURCES = "/api/validSources";

  @Test
  public void should_get_valid_sources_or_destinations_by_program_and_facilityType()
      throws Exception {
    //given
    ValidSourceDestinationDto sourceDestination = createValidSourceDestinationDto();

    UUID program = UUID.randomUUID();
    UUID facilityType = UUID.randomUUID();
    when(validSourceDestinationService.findSources(program, facilityType))
        .thenReturn(singletonList(sourceDestination));

    when(validSourceDestinationService.findDestinations(program, facilityType))
        .thenReturn(singletonList(sourceDestination));

    //1. perform valid destinations
    performSourcesOrDestinations(program, facilityType, sourceDestination, API_VALID_DESTINATIONS);

    //2. perform valid sources
    performSourcesOrDestinations(program, facilityType, sourceDestination, API_VALID_SOURCES);
  }

  private void performSourcesOrDestinations(
      UUID programId, UUID facilityTypeId,
      ValidSourceDestinationDto sourceDestinationDto, String uri) throws Exception {
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(uri)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", programId.toString())
        .param("facilityType", facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(sourceDestinationDto.getId().toString())))
        .andExpect(jsonPath("$[0].name", is(sourceDestinationDto.getName())))
        .andExpect(jsonPath("$[0].isFreeTextAllowed", is(true)));
  }

  @Test
  public void should_return_400_when_program_and_facilityType_not_found_in_ref_data()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doThrow(new ValidationMessageException(
        new Message(ERROR_PROGRAM_NOT_FOUND, programId.toString())))
        .when(validSourceDestinationService)
        .findDestinations(programId, facilityTypeId);

    //when
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(API_VALID_DESTINATIONS)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", programId.toString())
        .param("facilityType", facilityTypeId.toString()));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  private ValidSourceDestinationDto createValidSourceDestinationDto() {
    ValidSourceDestinationDto destinationAssignmentDto = new ValidSourceDestinationDto();
    destinationAssignmentDto.setId(UUID.randomUUID());
    destinationAssignmentDto.setName("CHW");
    destinationAssignmentDto.setIsFreeTextAllowed(true);
    return destinationAssignmentDto;
  }
}