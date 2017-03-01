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

package org.openlmis.stockmanagement.service;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.SupportedProgramDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.util.AuthenticationHelper;

import java.util.Arrays;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ProgramFacilityTypePermissionServiceTest {

  @InjectMocks
  private ProgramFacilityTypePermissionService programFacilityTypePermissionService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Test(expected = PermissionMessageException.class)
  public void throw_exception_when_facility_type_and_home_facility_type_not_match()
      throws Exception {
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();

    UserDto userDto = createUserDto(programId, facilityTypeId);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    programFacilityTypePermissionService.checkProgramFacility(programId, randomUUID());
  }

  @Test(expected = PermissionMessageException.class)
  public void throw_exception_when_program_is_not_supported_by_the_facility() throws Exception {
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();

    UserDto userDto = createUserDto(randomUUID(), facilityTypeId);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    programFacilityTypePermissionService.checkProgramFacility(programId, facilityTypeId);
  }

  @Test
  public void check_program_facility_happy_path() throws Exception {
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();

    UserDto userDto = createUserDto(programId, facilityTypeId);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    programFacilityTypePermissionService.checkProgramFacility(programId, facilityTypeId);
  }

  private UserDto createUserDto(UUID programId, UUID facilityTypeId) {
    UserDto userDto = new UserDto();

    FacilityDto homeFacility = new FacilityDto();
    userDto.setHomeFacility(homeFacility);

    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(facilityTypeId);
    homeFacility.setType(facilityTypeDto);

    SupportedProgramDto supportedProgramDto = new SupportedProgramDto();
    supportedProgramDto.setId(programId);
    homeFacility.setSupportedPrograms(Arrays.asList(supportedProgramDto));

    return userDto;
  }
}