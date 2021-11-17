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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.ValidDestinationAssignmentDataBuilder.createDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceAssignmentDataBuilder.createSource;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationDataBuilder.createFacilityDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationDataBuilder.createFacilityDestinationWithGeoLevelAffinity;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationDataBuilder.createFacilitySourceAssignment;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationDataBuilder.createOrganizationDestination;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationDataBuilder.createOrganizationSourceAssignment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.Organization;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.referencedata.GeographicLevelDto;
import org.openlmis.stockmanagement.dto.referencedata.GeographicZoneDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.testutils.GeographicLevelDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.GeographicZoneDtoDataBuilder;
import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class SourceDestinationBaseServiceTest {

  private static final String FACILITY_NAME = "Facility Name";
  private static final String ORGANIZATION_NAME = "NGO No 1";

  private static final String ORGANIZATION_NODE_NAME = "NGO";
  private static final String FACILITY_NODE_NAME = "Health Center";

  private Pageable pageRequest = PageRequest.of(1,200);

  @InjectMocks
  private ValidSourceService validSourceService;

  @InjectMocks
  private ValidDestinationService validDestinationService;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Mock
  private ValidDestinationAssignmentRepository destinationRepository;

  @Mock
  private ValidSourceAssignmentRepository sourceRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private NodeRepository nodeRepository;

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationExceptionWhenProgramAndFacilityTypeNotFound()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityId = randomUUID();
    UUID facilityTypeId = randomUUID();
    FacilityDto facilityDto = createFacilityDtoWithFacilityType(facilityId, facilityTypeId);
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);
    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    //when
    validSourceService.findSources(programId, facilityId, pageRequest);
  }

  @Test
  public void shouldReturnSourceDtoWhenFoundExistingOne() throws Exception {
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
    verify(programFacilityTypeExistenceService, times(1))
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
  }

  @Test
  public void shouldReturnSourceAssignmentWhenSourceIsAFacilityWithoutNode()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    when(sourceRepository.save(any(ValidSourceAssignment.class))).thenReturn(
        createSourceAssignment(programId, facilityTypeId, createNode(sourceId, true)));
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName(FACILITY_NAME);
    when(facilityReferenceDataService.exists(sourceId)).thenReturn(true);
    when(facilityReferenceDataService.findOne(sourceId)).thenReturn(facilityDto);
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    //when
    ValidSourceDestinationDto assignmentDto = validSourceService.assignSource(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(false));
    assertThat(assignmentDto.getName(), is(FACILITY_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(sourceId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(true));
  }

  @Test
  public void shouldReturnSourceAssignmentWhenSourceIsAnOrganization() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();

    when(sourceRepository.save(any(ValidSourceAssignment.class))).thenReturn(
        createSourceAssignment(programId, facilityTypeId, createNode(sourceId, false)));
    Organization organization = new Organization();
    organization.setName(ORGANIZATION_NAME);
    when(organizationRepository.existsById(sourceId)).thenReturn(true);
    when(organizationRepository.findById(sourceId)).thenReturn(Optional.of(organization));
    when(nodeRepository.findByReferenceId(sourceId)).thenReturn(null);
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    //when
    ValidSourceDestinationDto assignmentDto = validSourceService.assignSource(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(true));
    assertThat(assignmentDto.getName(), is(ORGANIZATION_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(sourceId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldReturn400WhenSourceNotFound() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID sourceId = randomUUID();
    ValidSourceAssignment assignment = createSource(programId, facilityTypeId, sourceId);

    //when
    validSourceService.assignSource(assignment);
  }

  @Test
  public void shouldReturnDestinationDtoWhenFoundExistingOne() throws Exception {
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
    verify(programFacilityTypeExistenceService, times(1))
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
  }

  @Test
  public void shouldReturnDestinationAssignmentWhenDestinationIsA_facilityWithoutNode()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();

    when(destinationRepository.save(any(ValidDestinationAssignment.class))).thenReturn(
        createDestinationAssignment(programId, facilityTypeId, createNode(destinationId, true)));
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setName(FACILITY_NAME);
    when(facilityReferenceDataService.exists(destinationId)).thenReturn(true);
    when(facilityReferenceDataService.findOne(destinationId)).thenReturn(facilityDto);
    when(nodeRepository.findByReferenceId(destinationId)).thenReturn(null);
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    //when
    ValidSourceDestinationDto assignmentDto = validDestinationService.assignDestination(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(false));
    assertThat(assignmentDto.getName(), is(FACILITY_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(destinationId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(true));
  }

  @Test
  public void shouldReturnDestinationAssignmentWhenDestinationIsA_organization()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();

    when(destinationRepository.save(any(ValidDestinationAssignment.class))).thenReturn(
        createDestinationAssignment(programId, facilityTypeId, createNode(destinationId, false)));
    Organization organization = new Organization();
    organization.setName(ORGANIZATION_NAME);
    when(organizationRepository.existsById(destinationId)).thenReturn(true);
    when(organizationRepository.findById(destinationId)).thenReturn(Optional.of(organization));
    when(nodeRepository.findByReferenceId(destinationId)).thenReturn(null);
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    //when
    ValidSourceDestinationDto assignmentDto = validDestinationService.assignDestination(assignment);

    //then
    assertThat(assignmentDto.getProgramId(), is(programId));
    assertThat(assignmentDto.getFacilityTypeId(), is(facilityTypeId));
    assertThat(assignmentDto.getIsFreeTextAllowed(), is(true));
    assertThat(assignmentDto.getName(), is(ORGANIZATION_NAME));
    assertThat(assignmentDto.getNode().getReferenceId(), is(destinationId));
    assertThat(assignmentDto.getNode().isRefDataFacility(), is(false));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldReturn400WhenDestinationNotFound() throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID destinationId = randomUUID();
    ValidDestinationAssignment assignment = createDestination(
        programId, facilityTypeId, destinationId);

    //when
    validDestinationService.assignDestination(assignment);
  }

  @Test
  public void
      shouldReturnListOfAllDestinationDtosWhenFindingValidDestinationAssignmentWithoutParams()
      throws Exception {
    //given
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    FacilityDto facilityDto = createFacilityDtoWithFacilityType(facilityId, facilityTypeId);
    facilityDto.setName(FACILITY_NODE_NAME);

    Map<UUID, FacilityDto> facilityMap = new HashMap<>();
    facilityMap.put(facilityId, facilityDto);

    when(facilityReferenceDataService.findByIds(Collections.singletonList(facilityId)))
            .thenReturn(facilityMap);

    List<ValidDestinationAssignment> validDestinationAssignments = asList(
            createOrganizationDestination(mockedOrganizationNode(ORGANIZATION_NODE_NAME)),
            createFacilityDestination(mockedFacilityNode(facilityId, FACILITY_NODE_NAME)));

    when(destinationRepository.findAll(pageRequest))
            .thenReturn(Pagination.getPage(validDestinationAssignments));

    //when
    Page<ValidSourceDestinationDto> validDestinations =
            validDestinationService.findDestinations(null, null, pageRequest);

    //then
    assertThat(validDestinations.getContent().size(), is(2));

    ValidSourceDestinationDto organization = validDestinations.getContent().get(0);
    assertThat(organization.getName(), is(ORGANIZATION_NODE_NAME));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validDestinations.getContent().get(1);
    assertThat(facility.getName(), is(FACILITY_NODE_NAME));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void shouldReturnListOfDestinationDtosWhenFindValidDestinationAssignment()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    FacilityDto facilityDto = createFacilityDtoWithFacilityType(facilityId, facilityTypeId);
    facilityDto.setName(FACILITY_NODE_NAME);

    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);

    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    List<ValidDestinationAssignment> validDestinationAssignments = asList(
        createOrganizationDestination(mockedOrganizationNode(ORGANIZATION_NODE_NAME)),
        createFacilityDestination(mockedFacilityNode(facilityId, FACILITY_NODE_NAME)));

    when(destinationRepository.findByProgramIdAndFacilityTypeId(
            programId, facilityTypeId, pageRequest))
        .thenReturn(validDestinationAssignments);

    when(facilityReferenceDataService.findByIds(anyListOf(UUID.class))).thenReturn(
        Collections.singletonMap(facilityId, facilityDto));

    //when
    Page<ValidSourceDestinationDto> validDestinations =
        validDestinationService.findDestinations(programId, facilityId, pageRequest);

    //then
    assertThat(validDestinations.getContent().size(), is(2));

    ValidSourceDestinationDto organization = validDestinations.getContent().get(0);
    assertThat(organization.getName(), is(ORGANIZATION_NODE_NAME));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validDestinations.getContent().get(1);
    assertThat(facility.getName(), is(FACILITY_NODE_NAME));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void shouldReturnListOfAllSourcesDtosWhenFindingValidSourcesAssignmentWithoutParams()
          throws Exception {
    //given
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    FacilityDto facilityDto = createFacilityDtoWithFacilityType(facilityId, facilityTypeId);
    facilityDto.setName(FACILITY_NODE_NAME);

    Map<UUID, FacilityDto> facilityMap = new HashMap<>();
    facilityMap.put(facilityId, facilityDto);

    when(facilityReferenceDataService.findByIds(Collections.singletonList(facilityId)))
            .thenReturn(facilityMap);

    List<ValidSourceAssignment> validSourceAssignments = asList(
            createOrganizationSourceAssignment(mockedOrganizationNode(ORGANIZATION_NODE_NAME)),
            createFacilitySourceAssignment(mockedFacilityNode(facilityId, FACILITY_NODE_NAME)));

    when(sourceRepository.findAll(pageRequest))
            .thenReturn(Pagination.getPage(validSourceAssignments));

    //when
    Page<ValidSourceDestinationDto> validSources =
            validSourceService.findSources(null, null, pageRequest);

    //then
    assertThat(validSources.getContent().size(), is(2));

    ValidSourceDestinationDto organization = validSources.getContent().get(0);
    assertThat(organization.getName(), is(ORGANIZATION_NODE_NAME));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validSources.getContent().get(1);
    assertThat(facility.getName(), is(FACILITY_NODE_NAME));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void shouldReturnListOfSourceDtosWhenFindingValidSourceAssignment()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    FacilityDto facilityDto = createFacilityDtoWithFacilityType(facilityId, facilityTypeId);
    facilityDto.setName(FACILITY_NODE_NAME);

    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);
    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    List<ValidSourceAssignment> validSourceAssignments = asList(
        createOrganizationSourceAssignment(mockedOrganizationNode(ORGANIZATION_NODE_NAME)),
        createFacilitySourceAssignment(mockedFacilityNode(facilityId, FACILITY_NODE_NAME)));

    when(sourceRepository.findByProgramIdAndFacilityTypeId(
            programId, facilityTypeId, pageRequest))
        .thenReturn(validSourceAssignments);

    when(facilityReferenceDataService.findByIds(anyListOf(UUID.class))).thenReturn(
        Collections.singletonMap(facilityId, facilityDto));

    //when
    Page<ValidSourceDestinationDto> validSources =
        validSourceService.findSources(programId, facilityId, pageRequest);

    //then
    assertThat(validSources.getContent().size(), is(2));

    ValidSourceDestinationDto organization = validSources.getContent().get(0);
    assertThat(organization.getName(), is(ORGANIZATION_NODE_NAME));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validSources.getContent().get(1);
    assertThat(facility.getName(), is(FACILITY_NODE_NAME));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void shouldReturnListOfDestinationDtosWhenGeoLevelAffinityMatch()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    UUID regionGeoLevelId = randomUUID();
    UUID regionGeoZoneId = randomUUID();
    UUID geoLevelAffinity = regionGeoLevelId;
    mockValidDestinationsAndFacilitiesWithGeoZonesAndLevel(programId, facilityTypeId, facilityId,
        regionGeoLevelId, regionGeoZoneId, geoLevelAffinity);

    //when
    Page<ValidSourceDestinationDto> validDestinations =
        validDestinationService.findDestinations(programId, facilityId, pageRequest);

    //then
    assertThat(validDestinations.getContent().size(), is(2));

    ValidSourceDestinationDto organization = validDestinations.getContent().get(0);
    assertThat(organization.getName(), is(ORGANIZATION_NODE_NAME));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validDestinations.getContent().get(1);
    assertThat(facility.getName(), is(FACILITY_NODE_NAME));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void shouldReturnListOfDestinationDtosWhenGeoLevelAffinitIsNull()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    UUID regionGeoLevelId = randomUUID();
    UUID regionGeoZoneId = randomUUID();
    UUID geoLevelAffinity = null;
    mockValidDestinationsAndFacilitiesWithGeoZonesAndLevel(programId, facilityTypeId, facilityId,
        regionGeoLevelId, regionGeoZoneId, geoLevelAffinity);

    //when
    Page<ValidSourceDestinationDto> validDestinations =
        validDestinationService.findDestinations(programId, facilityId, pageRequest);

    //then
    assertThat(validDestinations.getContent().size(), is(2));

    ValidSourceDestinationDto organization = validDestinations.getContent().get(0);
    assertThat(organization.getName(), is(ORGANIZATION_NODE_NAME));
    assertThat(organization.getIsFreeTextAllowed(), is(true));

    ValidSourceDestinationDto facility = validDestinations.getContent().get(1);
    assertThat(facility.getName(), is(FACILITY_NODE_NAME));
    assertThat(facility.getIsFreeTextAllowed(), is(false));
  }

  @Test
  public void shouldReturnListOfDestinationDtosWithOnlyOrganizationWhenGeoLevelAffinityNotMatch()
      throws Exception {
    //given
    UUID programId = randomUUID();
    UUID facilityTypeId = randomUUID();
    UUID facilityId = randomUUID();
    UUID regionGeoLevelId = randomUUID();
    UUID regionGeoZoneId = randomUUID();
    UUID geoLevelAffinity = randomUUID();
    mockValidDestinationsAndFacilitiesWithGeoZonesAndLevel(programId, facilityTypeId, facilityId,
        regionGeoLevelId, regionGeoZoneId, geoLevelAffinity);

    //when
    Page<ValidSourceDestinationDto> validDestinations =
        validDestinationService.findDestinations(programId, facilityId, pageRequest);

    //then
    assertThat(validDestinations.getContent().size(), is(1));
  }


  private void mockValidDestinationsAndFacilitiesWithGeoZonesAndLevel(UUID programId,
      UUID facilityTypeId, UUID facilityId, UUID regionGeoLevelId, UUID regionGeoZoneId,
      UUID geoLevelAffinity) {
    FacilityDto facilityDto = createFacilityDtoWithFacilityType(facilityId, facilityTypeId);

    facilityDto.setGeographicZone(generateGeographicZone(randomUUID(), regionGeoLevelId,
        randomUUID(), randomUUID(), regionGeoZoneId, randomUUID()));

    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);

    doNothing().when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    UUID refDataFacilityId = randomUUID();
    List<ValidDestinationAssignment> validDestinationAssignments = asList(
        createOrganizationDestination(mockedOrganizationNode(ORGANIZATION_NODE_NAME)),
        createFacilityDestinationWithGeoLevelAffinity(mockedFacilityNode(refDataFacilityId,
            FACILITY_NODE_NAME), geoLevelAffinity));

    when(destinationRepository.findByProgramIdAndFacilityTypeId(
            programId, facilityTypeId, pageRequest))
        .thenReturn(validDestinationAssignments);

    FacilityDto refDataFacilityDto = createFacilityDtoWithFacilityType(refDataFacilityId,
        facilityTypeId);
    refDataFacilityDto.setName(FACILITY_NODE_NAME);
    refDataFacilityDto.setGeographicZone(generateGeographicZone(randomUUID(), regionGeoLevelId,
        randomUUID(), randomUUID(), regionGeoZoneId, randomUUID()));

    when(facilityReferenceDataService.findByIds(anyListOf(UUID.class))).thenReturn(
        Collections.singletonMap(refDataFacilityId, refDataFacilityDto));
  }


  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenDeleteSourceAssignmentNotExists()
      throws Exception {
    UUID assignmentId = randomUUID();
    when(sourceRepository.existsById(assignmentId)).thenReturn(false);
    validSourceService.deleteSourceAssignmentById(assignmentId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenDeleteDestinationAssignmentNotExists()
      throws Exception {
    UUID assignmentId = randomUUID();
    when(destinationRepository.existsById(assignmentId)).thenReturn(false);
    validDestinationService.deleteDestinationAssignmentById(assignmentId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenFacilityNotExists()
      throws Exception {
    when(facilityReferenceDataService.findOne(any(UUID.class))).thenReturn(null);
    validDestinationService.findDestinations(randomUUID(), randomUUID(), pageRequest);
  }

  @Test
  public void shouldDeleteSourceAssignmentById() throws Exception {
    //given
    UUID assignmentId = randomUUID();
    when(sourceRepository.existsById(assignmentId)).thenReturn(true);

    //when
    validSourceService.deleteSourceAssignmentById(assignmentId);

    //then
    verify(sourceRepository, times(1)).deleteById(assignmentId);
  }

  @Test
  public void shouldDeleteDestinationAssignmentById() throws Exception {
    //given
    UUID assignmentId = randomUUID();
    when(destinationRepository.existsById(assignmentId)).thenReturn(true);

    //when
    validDestinationService.deleteDestinationAssignmentById(assignmentId);

    //then
    verify(destinationRepository, times(1)).deleteById(assignmentId);
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

  private GeographicZoneDto generateGeographicZone(UUID districtLevelId, UUID regionLevelId,
      UUID countryLevelId, UUID districtId, UUID regionId, UUID countryId) {

    GeographicLevelDto districtLevel = new GeographicLevelDtoDataBuilder().withId(districtLevelId)
        .build();
    GeographicLevelDto regionLevel = new GeographicLevelDtoDataBuilder().withId(regionLevelId)
        .build();
    GeographicLevelDto countryLevel = new GeographicLevelDtoDataBuilder().withId(countryLevelId)
        .build();

    GeographicZoneDto districtZone = new GeographicZoneDtoDataBuilder().withId(districtId)
        .withLevel(districtLevel).build();
    GeographicZoneDto regionZone = new GeographicZoneDtoDataBuilder().withId(regionId)
        .withLevel(regionLevel).build();
    GeographicZoneDto countryZone = new GeographicZoneDtoDataBuilder().withId(countryId)
        .withLevel(countryLevel).build();

    regionZone.setParent(countryZone);
    districtZone.setParent(regionZone);

    return districtZone;
  }

  private FacilityDto createFacilityDtoWithFacilityType(UUID facilityId, UUID facilityTypeId) {
    FacilityTypeDto facilityDto = new FacilityTypeDto();
    facilityDto.setId(facilityTypeId);
    return FacilityDto.builder()
        .id(facilityId)
        .type(facilityDto)
        .build();
  }

  private Node mockedFacilityNode(UUID facilityId, String name) {
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setId(facilityId);
    facilityDto.setName(name);
    facilityDto.setId(facilityId);

    Node node = new Node();
    node.setRefDataFacility(true);
    node.setReferenceId(facilityDto.getId());
    return node;
  }

  private Node mockedOrganizationNode(String name) {
    Organization organization = new Organization();
    organization.setName(name);
    organization.setId(randomUUID());
    when(organizationRepository.findById(organization.getId()))
        .thenReturn(Optional.of(organization));

    Node node = new Node();
    node.setRefDataFacility(false);
    node.setId(randomUUID());
    node.setReferenceId(organization.getId());
    return node;
  }
}