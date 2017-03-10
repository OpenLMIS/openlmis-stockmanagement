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
import static org.openlmis.stockmanagement.testutils.ValidDestinationAssignmentBuilder.createDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceAssignmentBuilder.createSource;
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
public class SourceDestinationBaseServiceTest {

  private static final String FACILITY_NAME = "Facility Name";
  private static final String ORGANIZATION_NAME = "NGO No 1";

  @InjectMocks
  private ValidSourceService validSourceService;

  @InjectMocks
  private ValidDestinationService validDestinationService;

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
    validSourceService.findSources(programId, facilityTypeId);
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
    validSourceService.findSources(programId, facilityTypeId);
  }

  @Test
  public void should_return_source_dto_when_found_existing_one() throws Exception {
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);
    Node node = createNode(sourceId, true);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(node);
    when(facilityReferenceDataService.findOne(sourceId)).thenReturn(new FacilityDto());

    when(sourceRepository.findByProgramIdAndFacilityTypeIdAndNodeId(
        programId, facilityTypeId, node.getId()))
        .thenReturn(createSourceAssignment(programId, facilityTypeId, node));

    //when
    ValidSourceDestinationDto foundDto = validSourceService
        .findByProgramFacilitySource(assignment);

    assertThat(foundDto.getProgramId(), is(programId));
    assertThat(foundDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(foundDto.getNode().getReferenceId(), is(sourceId));
    verify(permissionService, times(1)).canManageStockSources();
    verify(programFacilityTypeExistenceService, times(1))
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
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
    facilityDto.setName(FACILITY_NAME);
    when(facilityReferenceDataService.findOne(sourceId)).thenReturn(facilityDto);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    //when
    ValidSourceDestinationDto assignmentDto = validSourceService
        .assignSource(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(false));
    assertThat(assignmentDto.getName(), is(FACILITY_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(sourceId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(true));
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
    organization.setName(ORGANIZATION_NAME);
    when(organizationRepository.findOne(sourceId)).thenReturn(organization);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    //when
    ValidSourceDestinationDto assignmentDto = validSourceService
        .assignSource(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(true));
    assertThat(assignmentDto.getName(), is(ORGANIZATION_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(sourceId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void should_return_400_when_source_not_found() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);
    when(organizationRepository.findOne(sourceId)).thenReturn(null);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);

    //when
    validSourceService.assignSource(assignment);
  }

  @Test
  public void should_return_destination_dto_when_found_existing_one() throws Exception {
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);
    Node node = createNode(destinationId, true);
    when(nodeRepository.findByReferenceId(destinationId)).thenReturn(node);
    when(facilityReferenceDataService.findOne(destinationId)).thenReturn(new FacilityDto());

    when(destinationRepository.findByProgramIdAndFacilityTypeIdAndNodeId(
        programId, facilityTypeId, node.getId()))
        .thenReturn(createDestinationAssignment(programId, facilityTypeId, node));

    //when
    ValidSourceDestinationDto foundDto = validDestinationService
        .findByProgramFacilityDestination(assignment);

    assertThat(foundDto.getProgramId(), is(programId));
    assertThat(foundDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(foundDto.getNode().getReferenceId(), is(destinationId));
    verify(permissionService, times(1)).canManageStockDestinations();
    verify(programFacilityTypeExistenceService, times(1))
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
  }

  @Test
  public void should_return_destination_assignment_when_destination_is_a_facility_without_node()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();

    when(destinationRepository.save(any(ValidDestinationAssignment.class))).thenReturn(
        createDestinationAssignment(programId, facilityTypeId, createNode(destinationId, true)));
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName(FACILITY_NAME);
    when(facilityReferenceDataService.findOne(destinationId)).thenReturn(facilityDto);
    when(nodeRepository.findByReferenceId(destinationId)).thenReturn(null);
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    //when
    ValidSourceDestinationDto assignmentDto = validDestinationService
        .assignDestination(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(false));
    assertThat(assignmentDto.getName(), is(FACILITY_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(destinationId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(true));
  }

  @Test
  public void should_return_destination_assignment_when_destination_is_a_organization()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();

    when(destinationRepository.save(any(ValidDestinationAssignment.class))).thenReturn(
        createDestinationAssignment(programId, facilityTypeId, createNode(destinationId, false)));
    Organization organization = new Organization();
    organization.setName(ORGANIZATION_NAME);
    when(organizationRepository.findOne(destinationId)).thenReturn(organization);
    when(nodeRepository.findByReferenceId(destinationId)).thenReturn(null);
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    //when
    ValidSourceDestinationDto assignmentDto = validDestinationService
        .assignDestination(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(true));
    assertThat(assignmentDto.getName(), is(ORGANIZATION_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(destinationId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void should_return_400_when_destination_not_found() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);
    when(organizationRepository.findOne(destinationId)).thenReturn(null);
    when(nodeRepository.findByReferenceId(destinationId)).thenReturn(null);

    //when
    validDestinationService.assignDestination(assignment);
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
    validDestinationService.findDestinations(programId, facilityTypeId);
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
        validDestinationService.findDestinations(programId, facilityTypeId);

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
        validSourceService.findSources(programId, facilityTypeId);

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
        .when(permissionService).canManageStockSources();
    validSourceService.deleteSourceAssignmentById(randomUUID());
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_exception_when_delete_source_assignment_not_exists()
      throws Exception {
    UUID assignmentId = randomUUID();
    when(sourceRepository.exists(assignmentId)).thenReturn(false);
    validSourceService.deleteSourceAssignmentById(assignmentId);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_exception_when_delete_destination_assignment_not_exists()
      throws Exception {
    UUID assignmentId = randomUUID();
    when(destinationRepository.exists(assignmentId)).thenReturn(false);
    validDestinationService.deleteDestinationAssignmentById(assignmentId);
  }

  @Test
  public void should_delete_source_assignment_by_id() throws Exception {
    //given
    UUID assignmentId = randomUUID();
    when(sourceRepository.exists(assignmentId)).thenReturn(true);

    //when
    validSourceService.deleteSourceAssignmentById(assignmentId);

    //then
    verify(sourceRepository, times(1)).delete(assignmentId);
  }

  @Test
  public void should_delete_destination_assignment_by_id() throws Exception {
    //given
    UUID assignmentId = randomUUID();
    when(destinationRepository.exists(assignmentId)).thenReturn(true);

    //when
    validDestinationService.deleteDestinationAssignmentById(assignmentId);

    //then
    verify(destinationRepository, times(1)).delete(assignmentId);
  }

  private Node createNode(UUID referenceId, boolean isRefDataFacility) {
    Node node = new Node();
    node.setReferenceId(referenceId);
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

  private ValidDestinationAssignment createDestinationAssignment(
      UUID programId, UUID facilityTypeId, Node node) {
    ValidDestinationAssignment assignment = new ValidDestinationAssignment();
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