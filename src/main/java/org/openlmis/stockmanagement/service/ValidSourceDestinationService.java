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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_ASSIGNMENT_NOT_FOUND;

import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidSourceDestinationService {

  @Autowired
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationRepository;

  @Autowired
  private ValidSourceAssignmentRepository validSourceRepository;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private NodeRepository nodeRepository;

  /**
   * Find valid destinations by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid destination assignment DTOs
   */
  public List<ValidSourceDestinationDto> findSources(
      UUID programId, UUID facilityTypeId) {
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    permissionService.canViewStockSource(programId, facilityTypeId);

    List<ValidSourceAssignment> sourceAssignments =
        validSourceRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    return sourceAssignments.stream()
        .map(source -> createDtoFrom(source.getNode()))
        .collect(Collectors.toList());
  }

  /**
   * Find valid sources by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid source assignment DTOs
   */
  public List<ValidSourceDestinationDto> findDestinations(UUID programId, UUID facilityTypeId) {
    programFacilityTypeExistenceService
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    permissionService
        .canViewStockDestinations(programId, facilityTypeId);

    List<ValidDestinationAssignment> destinationAssignments =
        validDestinationRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    return destinationAssignments.stream()
        .map(destination -> createDtoFrom(destination.getNode()))
        .collect(Collectors.toList());
  }

  /**
   * Assign a source to a program and facility type.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   * @param sourceId     source ID
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto assignSource(UUID program, UUID facilityType, UUID sourceId) {
    permissionService.canManageStockSource();
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(program, facilityType);

    if (facilityRefDataService.findOne(sourceId) != null) {
      return createFrom(createAssignment(program, facilityType, findOrCreateNode(sourceId, true)));
    } else if (organizationRepository.findOne(sourceId) != null) {
      return createFrom(createAssignment(program, facilityType, findOrCreateNode(sourceId, false)));
    }

    throw new ValidationMessageException(new Message(MessageKeys.ERROR_SOURCE_NOT_FOUND));
  }

  private ValidSourceAssignment createAssignment(UUID program, UUID facilityType, Node node) {
    ValidSourceAssignment assignment = new ValidSourceAssignment();
    assignment.setProgramId(program);
    assignment.setFacilityTypeId(facilityType);
    assignment.setNode(node);
    return validSourceRepository.save(assignment);
  }

  /**
   * Find existing source assignment.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @param sourceId       source ID
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto findByProgramFacilitySource(
      UUID programId, UUID facilityTypeId, UUID sourceId) {
    Node foundNode = nodeRepository.findByReferenceId(sourceId);
    if (foundNode != null) {
      ValidSourceAssignment foundAssignment =
          validSourceRepository.findByProgramIdAndFacilityTypeIdAndNodeId(
              programId, facilityTypeId, foundNode.getId());

      if (foundAssignment != null) {
        return createFrom(foundAssignment);
      }
    }

    return null;
  }

  /**
   * Delete a source assignment by Id.
   *
   * @param assignmentId source assignment Id
   */
  public void deleteSourceAssignmentById(UUID assignmentId) {
    permissionService.canManageStockSource();
    checkSourceAssignmentIdExists(assignmentId);
    validSourceRepository.delete(assignmentId);
  }

  private void checkSourceAssignmentIdExists(UUID sourceAssignmentId) {
    if (!validSourceRepository.exists(sourceAssignmentId)) {
      throw new ValidationMessageException(new Message(ERROR_SOURCE_ASSIGNMENT_NOT_FOUND));
    }
  }

  private Node findOrCreateNode(UUID sourceId, boolean isRefDataFacility) {
    Node node = nodeRepository.findByReferenceId(sourceId);
    if (node == null) {
      node = new Node();
      node.setReferenceId(sourceId);
      node.setRefDataFacility(isRefDataFacility);
      return nodeRepository.save(node);
    }
    return node;
  }

  private ValidSourceDestinationDto createDtoFrom(Node node) {
    ValidSourceDestinationDto dto = new ValidSourceDestinationDto();

    dto.setId(node.getId());
    boolean isRefDataFacility = node.isRefDataFacility();
    if (isRefDataFacility) {
      dto.setName(facilityRefDataService.findOne(node.getReferenceId()).getName());
    } else {
      dto.setName(organizationRepository.findOne(node.getReferenceId()).getName());
    }
    dto.setIsFreeTextAllowed(!isRefDataFacility);
    return dto;
  }

  private ValidSourceDestinationDto createFrom(ValidSourceAssignment assignment) {
    Node node = assignment.getNode();

    ValidSourceDestinationDto dto = createDtoFrom(node);
    dto.setNode(node);
    dto.setAssignmentId(assignment.getId());
    dto.setProgramId(assignment.getProgramId());
    dto.setFacilityTypeId(assignment.getFacilityTypeId());
    return dto;
  }
}
