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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.Organization;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.dto.ValidDestinationAssignmentDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ValidSourceDestinationServiceTest {

  @InjectMocks
  private ValidSourceDestinationService validSourceDestinationService;

  @Mock
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Mock
  private ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_program_and_facilityType_not_found()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    validSourceDestinationService.findValidDestinations(programId, facilityTypeId);
  }

  @Test
  public void should_return_empty_list_when_valid_destination_assignment_not_found()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    List<ValidDestinationAssignmentDto> validDestinations =
        validSourceDestinationService.findValidDestinations(programId, facilityTypeId);

    //then
    assertThat(validDestinations.isEmpty(), is(true));
  }

  @Test
  public void should_return_list_of_destination_dtos_when_find_valid_destination_assignment()
      throws Exception {
    //given
    UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");
    UUID facilityTypeId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");
    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    ValidDestinationAssignment organizationDestination = mockOrganizationDestination();
    ValidDestinationAssignment facilityDestination = mockFacilityDestinationAssignment();
    when(validDestinationAssignmentRepository
        .findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(asList(organizationDestination, facilityDestination));

    //when
    List<ValidDestinationAssignmentDto> validDestinations =
        validSourceDestinationService.findValidDestinations(programId, facilityTypeId);

    //then
    assertThat(validDestinations.size(), is(2));
    assertThat(validDestinations.get(0).getName(), is("CHW"));
    assertThat(validDestinations.get(1).getName(), is("Balaka District Hospital"));
  }

  private ValidDestinationAssignment mockFacilityDestinationAssignment() {
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName("Balaka District Hospital");
    facilityDto.setId(UUID.randomUUID());

    Node node2 = new Node();
    node2.setRefDataFacility(true);
    node2.setReferenceId(facilityDto.getId());

    ValidDestinationAssignment destination2 = new ValidDestinationAssignment();
    destination2.setNode(node2);
    when(facilityReferenceDataService.findOne(facilityDto.getId())).thenReturn(facilityDto);
    return destination2;
  }

  private ValidDestinationAssignment mockOrganizationDestination() {
    Organization organization = new Organization();
    organization.setName("CHW");
    organization.setId(UUID.randomUUID());

    Node node = new Node();
    node.setRefDataFacility(false);
    node.setId(UUID.randomUUID());
    node.setReferenceId(organization.getId());

    ValidDestinationAssignment destination = new ValidDestinationAssignment();
    destination.setNode(node);
    when(organizationRepository.findOne(organization.getId())).thenReturn(organization);
    return destination;
  }
}