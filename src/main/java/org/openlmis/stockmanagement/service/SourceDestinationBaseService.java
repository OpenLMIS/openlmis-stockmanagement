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

import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.SourceDestinationAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.GeographicZoneDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.SourceDestinationAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.openlmis.stockmanagement.dto.ValidSourceDestinationDto.createFrom;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_ASSIGNMENT_ID_MISSING;

@Service
public abstract class SourceDestinationBaseService {

  @Autowired
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private NodeRepository nodeRepository;

  /**
   * Delete an assignment.
   *
   * @param assignmentId assignment id
   * @param repository assignment repository
   * @param errorKey error message key
   * @param <T> assignment type
   */
  protected <T extends SourceDestinationAssignment> void doDelete(
      UUID assignmentId, SourceDestinationAssignmentRepository<T> repository, String errorKey) {
    if (!repository.exists(assignmentId)) {
      throw new ValidationMessageException(new Message(errorKey));
    }
    repository.delete(assignmentId);
  }

  /**
   * Try to find an existing assignment by id.
   * 
   * @param assignmentId assignment id
   * @param repository assignment repository
   * @param errorKey error message key
   * @param <T> assignment type
   * @return assigmnet dto 
   * @throws ValidationMessageException when assignment was not found
   */
  protected <T extends SourceDestinationAssignment> ValidSourceDestinationDto findById(
      UUID assignmentId, SourceDestinationAssignmentRepository<T> repository, String errorKey) {
    SourceDestinationAssignment assignment = repository.findOne(assignmentId);
    if (assignment == null) {
      throw new ValidationMessageException(new Message(errorKey));
    }
    return createAssignmentDto(assignment, null);
  }

  /**
   * Try to find an existing assignment.
   *
   * @param assignment source or destination assignment
   * @param repository assignment repository
   * @param <T> assignment type
   * @return assignment dto or null if not found.
   */
  protected <T extends SourceDestinationAssignment> ValidSourceDestinationDto findAssignment(
      T assignment, SourceDestinationAssignmentRepository<T> repository) {
    UUID programId = assignment.getProgramId();
    UUID facilityTypeId = assignment.getFacilityTypeId();
    Node foundNode = findExistingNode(assignment, programId, facilityTypeId);
    if (foundNode != null) {
      SourceDestinationAssignment foundAssignment = repository
          .findByProgramIdAndFacilityTypeIdAndNodeId(
              programId, facilityTypeId, foundNode.getId());

      if (foundAssignment != null) {
        return createAssignmentDto(foundAssignment, null);
      }
    }
    return null;
  }

  /**
   * Get a list of assignments.
   * This method will return only those assignments that match the geo level affinity
   * or all possible assignments (when filtering params are not provided).
   *
   * @param programId program id
   * @param facilityId facility id
   * @param repository assignment repository
   * @param <T> assignment type
   * @return a list of assignment dto or empty list if not found.
   */
  protected <T extends SourceDestinationAssignment> List<ValidSourceDestinationDto> findAssignments(
          UUID programId, UUID facilityId, SourceDestinationAssignmentRepository<T> repository) {
    boolean isFiltered = programId != null && facilityId != null;

    return isFiltered
        ? findFilteredAssignments(programId, facilityId, repository)
        : findAllAssignments(repository);
  }

  /**
   * Create a new assignment.
   *
   * @param assignment assignment
   * @param errorKey error message key
   * @param repository assignment repository
   * @param <T> assignment type
   * @return created assignment.
   */
  protected <T extends SourceDestinationAssignment> ValidSourceDestinationDto doAssign(
      T assignment, String errorKey, SourceDestinationAssignmentRepository<T> repository) {
    UUID referenceId = assignment.getNode().getReferenceId();
    boolean isRefFacility = facilityRefDataService.exists(referenceId);
    boolean isOrganization = organizationRepository.exists(referenceId);
    if (isRefFacility || isOrganization) {
      assignment.setNode(findOrCreateNode(referenceId, isRefFacility));
      return createAssignmentDto(repository.save(assignment), null);
    }
    throw new ValidationMessageException(new Message(errorKey));
  }

  private <T extends SourceDestinationAssignment> Node findExistingNode(
      T assignment, UUID programId, UUID facilityTypeId) {
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId,
        facilityTypeId);
    Node node = assignment.getNode();
    if (node == null || node.getReferenceId() == null) {
      throw new ValidationMessageException(
          new Message(ERROR_SOURCE_DESTINATION_ASSIGNMENT_ID_MISSING));
    }
    return nodeRepository.findByReferenceId(node.getReferenceId());
  }

  private Node findOrCreateNode(UUID referenceId, boolean isRefDataFacility) {
    Node node = nodeRepository.findByReferenceId(referenceId);
    if (node == null) {
      node = new Node();
      node.setReferenceId(referenceId);
      node.setRefDataFacility(isRefDataFacility);
      return nodeRepository.save(node);
    }
    return node;
  }


  private boolean hasGeoAffinity(SourceDestinationAssignment assignment, FacilityDto facility,
      Map<UUID, FacilityDto> facilitiesById) {

    //A null geoLevelAffinity would cause the system to work as-is. 
    if (assignment.getGeoLevelAffinityId() == null) {
      return true;
    }

    FacilityDto facilityDto = facilitiesById.get(assignment.getNode().getReferenceId());
    UUID geoLevelAffinity = assignment.getGeoLevelAffinityId();

    Map<UUID, UUID> facilityGeoLevelMap = getFacilityGeoLevelZoneMap(facility);
    Map<UUID, UUID> assignmentFacilityGeoLevelZoneMap = getFacilityGeoLevelZoneMap(facilityDto);

    if (!facilityGeoLevelMap.containsKey(geoLevelAffinity) 
        || !assignmentFacilityGeoLevelZoneMap.containsKey(geoLevelAffinity)) {
      return false;
    }

    return facilityGeoLevelMap.get(geoLevelAffinity)
        .equals(assignmentFacilityGeoLevelZoneMap.get(geoLevelAffinity));
  }

  private Map<UUID, UUID> getFacilityGeoLevelZoneMap(FacilityDto homeFacility) {
    Map<UUID, UUID> facilityGeoLevelZoneMap = new HashMap<>();
    GeographicZoneDto geographicZoneDto = homeFacility.getGeographicZone();

    while (geographicZoneDto != null) {
      facilityGeoLevelZoneMap.put(geographicZoneDto.getLevel().getId(), geographicZoneDto.getId());
      geographicZoneDto = geographicZoneDto.getParent();
    }
    return facilityGeoLevelZoneMap;
  }

  private ValidSourceDestinationDto createAssignmentDto(SourceDestinationAssignment assignment,
      Map<UUID, FacilityDto> facilitiesById) {
    UUID referenceId = assignment.getNode().getReferenceId();

    if (assignment.getNode().isRefDataFacility()) {
      if (facilitiesById != null) {
        return createFrom(assignment, facilitiesById.get(referenceId).getName());
      }
      return createFrom(assignment, facilityRefDataService.findOne(referenceId).getName());
    }
    return createFrom(assignment, organizationRepository.findOne(referenceId).getName());
  }

  private <T extends SourceDestinationAssignment> List<ValidSourceDestinationDto> findFilteredAssignments(
          UUID programId, UUID facilityId, SourceDestinationAssignmentRepository<T> repository) {
    FacilityDto facility = facilityRefDataService.findOne(facilityId);

    if (facility == null) {
      throw new ValidationMessageException(
              new Message(ERROR_FACILITY_NOT_FOUND, facilityId.toString()));
    }

    UUID facilityTypeId = facility.getType().getId();
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    List<T> assignments = repository
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    List<UUID> facilitiesIds = assignments.stream()
            .filter(assignment -> assignment.getNode().isRefDataFacility())
            .map(assignment -> assignment.getNode().getReferenceId())
            .collect(Collectors.toList());

    Map<UUID, FacilityDto> facilitiesById = facilityRefDataService.findByIds(facilitiesIds);

    List<SourceDestinationAssignment> geoAssigment = assignments.stream()
            .filter(assignment -> !assignment.getNode().isRefDataFacility()
                    || hasGeoAffinity(assignment, facility, facilitiesById))
            .collect(Collectors.toList());

    return geoAssigment.stream()
            .map(assignment -> createAssignmentDto(assignment, facilitiesById))
            .collect(Collectors.toList());
  }

  private <T extends SourceDestinationAssignment> List<ValidSourceDestinationDto> findAllAssignments(
          SourceDestinationAssignmentRepository<T> repository) {
    return repository.findAll().stream()
            .map(assignment -> createAssignmentDto(assignment, null))
            .collect(Collectors.toList());
  }
}
