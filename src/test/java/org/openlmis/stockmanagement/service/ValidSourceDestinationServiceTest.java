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
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
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

  @Mock
  private NodeRepository nodeRepository;

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_program_and_facilityType_not_found()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    validSourceDestinationService.findSources(programId, facilityTypeId);
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_when_user_has_no_permission_to_view_sources()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService)
        .canViewStockSource(programId, facilityTypeId);

    //when
    validSourceDestinationService.findSources(programId, facilityTypeId);
  }

  @Test
  public void should_return_source_dto_when_found_existing_one() throws Exception {
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    Node node = createNode(sourceId, true);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(node);
    when(facilityReferenceDataService.findOne(sourceId)).thenReturn(new FacilityDto());

    when(sourceRepository.findByProgramIdAndFacilityTypeIdAndNodeId(
        programId, facilityTypeId, node.getId()))
        .thenReturn(createSourceAssignment(programId, facilityTypeId, node));

    //when
    ValidSourceDestinationDto foundDto = validSourceDestinationService
        .findByProgramFacilitySource(programId, facilityTypeId, sourceId);

    assertThat(foundDto.getProgramId(), is(programId));
    assertThat(foundDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(foundDto.getNode().getReferenceId(), is(sourceId));
  }

  @Test
  public void should_return_source_assignment_when_source_is_a_facility_without_node()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    when(sourceRepository.save(any(ValidSourceAssignment.class))).thenReturn(
        createSourceAssignment(programId, facilityTypeId, createNode(sourceId, true)));
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName("Facility Name");
    when(facilityReferenceDataService.findOne(sourceId)).thenReturn(facilityDto);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);

    //when
    ValidSourceDestinationDto assignment = validSourceDestinationService
        .assignSource(programId, facilityTypeId, sourceId);

    //then
    assertThat(assignment.getProgramId(), is(programId));
    assertThat(assignment.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignment.getIsFreeTextAllowed(), is(false));
    assertThat(assignment.getName(), is("Facility Name"));
    assertThat(assignment.getNode().getReferenceId(), is(sourceId));
    assertThat(assignment.getNode().isRefDataFacility(), is(true));
  }

  @Test
  public void should_return_source_assignment_when_source_is_a_organization() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    when(sourceRepository.save(any(ValidSourceAssignment.class))).thenReturn(
        createSourceAssignment(programId, facilityTypeId, createNode(sourceId, false)));
    Organization organization = new Organization();
    organization.setName("NGO No 1");
    when(organizationRepository.findOne(sourceId)).thenReturn(organization);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);

    //when
    ValidSourceDestinationDto assignment = validSourceDestinationService
        .assignSource(programId, facilityTypeId, sourceId);

    //then
    assertThat(assignment.getProgramId(), is(programId));
    assertThat(assignment.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignment.getIsFreeTextAllowed(), is(true));
    assertThat(assignment.getName(), is("NGO No 1"));
    assertThat(assignment.getNode().getReferenceId(), is(sourceId));
    assertThat(assignment.getNode().isRefDataFacility(), is(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void should_return_400_when_source_not_found() throws Exception {
    //given
    UUID sourceId = randomUUID();

    when(organizationRepository.findOne(sourceId)).thenReturn(null);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);

    //when
    validSourceDestinationService.assignSource(randomUUID(), randomUUID(), sourceId);
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_when_user_has_no_permission_to_view_destinations()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
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
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
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
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
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

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_when_user_has_no_permission_to_delete_assignment()
      throws Exception {
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageStockSource();
    validSourceDestinationService.deleteSourceAssignmentById(randomUUID());
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_exception_when_delete_source_assignment_not_exists()
      throws Exception {
    UUID assignmentId = randomUUID();
    when(sourceRepository.exists(assignmentId)).thenReturn(false);
    validSourceDestinationService.deleteSourceAssignmentById(assignmentId);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_exception_when_delete_destination_assignment_not_exists()
      throws Exception {
    UUID assignmentId = randomUUID();
    when(sourceRepository.exists(assignmentId)).thenReturn(false);
    validSourceDestinationService.deleteDestinationAssignmentById(assignmentId);
  }

  @Test
  public void should_delete_source_assignment_by_id() throws Exception {
    //given
    UUID assignmentId = randomUUID();
    when(sourceRepository.exists(assignmentId)).thenReturn(true);

    //when
    validSourceDestinationService.deleteSourceAssignmentById(assignmentId);

    //then
    verify(sourceRepository, times(1)).delete(assignmentId);
  }

  @Test
  public void should_delete_destination_assignment_by_id() throws Exception {
    //given
    UUID assignmentId = randomUUID();
    when(destinationRepository.exists(assignmentId)).thenReturn(true);

    //when
    validSourceDestinationService.deleteDestinationAssignmentById(assignmentId);

    //then
    verify(destinationRepository, times(1)).delete(assignmentId);
  }

  private Node createNode(UUID sourceId, boolean isRefDataFacility) {
    Node node = new Node();
    node.setReferenceId(sourceId);
    node.setId(randomUUID());
    node.setRefDataFacility(isRefDataFacility);
    return node;
  }

  private ValidSourceAssignment createSourceAssignment(
      UUID programId, UUID facilityTypeId, Node node) {
    ValidSourceAssignment assignment = new ValidSourceAssignment();
    assignment.setNode(node);
    assignment.setProgramId(programId);
    assignment.setFacilityTypeId(facilityTypeId);
    return assignment;
  }

  private Node mockedFacilityNode(String name) {
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName(name);
    facilityDto.setId(randomUUID());
    when(facilityReferenceDataService.findOne(facilityDto.getId())).thenReturn(facilityDto);

    Node node = new Node();
    node.setRefDataFacility(true);
    node.setReferenceId(facilityDto.getId());
    return node;
  }

  private Node mockedOrganizationNode(String name) {
    Organization organization = new Organization();
    organization.setName(name);
    organization.setId(randomUUID());
    when(organizationRepository.findOne(organization.getId())).thenReturn(organization);

    Node node = new Node();
    node.setRefDataFacility(false);
    node.setId(randomUUID());
    node.setReferenceId(organization.getId());
    return node;
  }
}