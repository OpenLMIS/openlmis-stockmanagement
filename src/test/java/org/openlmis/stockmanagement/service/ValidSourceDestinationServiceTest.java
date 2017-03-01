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
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationBuilder.createFacilityDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationBuilder.createFacilitySourceAssignment;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationBuilder.createOrganizationDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationBuilder.createOrganizationSourceAssignment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.Organization;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ValidSourceDestinationServiceTest {

  @InjectMocks
  private ValidSourceDestinationService validSourceDestinationService;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private ValidDestinationAssignmentRepository destinationRepository;

  @Mock
  private ValidSourceAssignmentRepository sourceRepository;

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
    validSourceDestinationService.findSources(programId, facilityTypeId);
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_when_user_has_no_permission_to_view_sources()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService)
        .canViewStockSource(programId, facilityTypeId);

    //when
    validSourceDestinationService.findSources(programId, facilityTypeId);
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_when_user_has_no_permission_to_view_destinations()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService)
        .canViewStockDestinations(programId, facilityTypeId);

    //when
    validSourceDestinationService.findDestinations(programId, facilityTypeId);
  }

  @Test
  public void should_return_list_of_destination_dtos_when_find_valid_destination_assignment()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    List<ValidDestinationAssignment> validDestinationAssignments = asList(
        createOrganizationDestination(mockedOrganizationNode("CHW")),
        createFacilityDestination(mockedFacilityNode("Balaka District Hospital")));

    when(destinationRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(validDestinationAssignments);

    //when
    List<ValidSourceDestinationDto> validDestinations =
        validSourceDestinationService.findDestinations(programId, facilityTypeId);

    //then
    assertThat(validDestinations.size(), is(2));

    ValidSourceDestinationDto organization = validDestinations.get(0);
    assertThat(organization.getName(), is("CHW"));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validDestinations.get(1);
    assertThat(facility.getName(), is("Balaka District Hospital"));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void should_return_list_of_source_dtos_when_find_valid_source_assignment()
      throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityTypeId = UUID.randomUUID();
    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    List<ValidSourceAssignment> validSourceAssignments = asList(
        createOrganizationSourceAssignment(mockedOrganizationNode("NGO")),
        createFacilitySourceAssignment(mockedFacilityNode("Health Center")));

    when(sourceRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
        .thenReturn(validSourceAssignments);

    //when
    List<ValidSourceDestinationDto> validSources =
        validSourceDestinationService.findSources(programId, facilityTypeId);

    //then
    assertThat(validSources.size(), is(2));

    ValidSourceDestinationDto organization = validSources.get(0);
    assertThat(organization.getName(), is("NGO"));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validSources.get(1);
    assertThat(facility.getName(), is("Health Center"));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  private Node mockedFacilityNode(String name) {
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName(name);
    facilityDto.setId(UUID.randomUUID());
    when(facilityReferenceDataService.findOne(facilityDto.getId())).thenReturn(facilityDto);

    Node node = new Node();
    node.setRefDataFacility(true);
    node.setReferenceId(facilityDto.getId());
    return node;
  }

  private Node mockedOrganizationNode(String name) {
    Organization organization = new Organization();
    organization.setName(name);
    organization.setId(UUID.randomUUID());
    when(organizationRepository.findOne(organization.getId())).thenReturn(organization);

    Node node = new Node();
    node.setRefDataFacility(false);
    node.setId(UUID.randomUUID());
    node.setReferenceId(organization.getId());
    return node;
  }
}