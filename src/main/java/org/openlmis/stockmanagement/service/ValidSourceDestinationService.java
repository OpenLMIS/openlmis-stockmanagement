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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_ASSIGNMENT_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_ASSIGNMENT_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_FOUND;

import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.SourceDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.SourceDestinationAssignmentRepository;
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
@SuppressWarnings("PMD.TooManyMethods")
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
  public List<ValidSourceDestinationDto> findSources(UUID programId, UUID facilityTypeId) {
    permissionService.canViewStockSource(programId, facilityTypeId);
    return findAssignments(programId, facilityTypeId, validSourceRepository);
  }

  /**
   * Find valid sources by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid source assignment DTOs
   */
  public List<ValidSourceDestinationDto> findDestinations(UUID programId, UUID facilityTypeId) {
    permissionService.canViewStockDestinations(programId, facilityTypeId);
    return findAssignments(programId, facilityTypeId, validDestinationRepository);
  }

  /**
   * Assign a source to a program and facility type.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   * @param sourceId     source ID
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto assignSource(UUID program, UUID facilityType, UUID sourceId)
      throws InstantiationException, IllegalAccessException {

    permissionService.canManageStockSources();
    return doAssign(program, facilityType, sourceId,
        ERROR_SOURCE_NOT_FOUND, ValidSourceAssignment.class, validSourceRepository);
  }

  /**
   * Assign a destination to a program and facility type.
   *
   * @param program       program ID
   * @param facilityType  facility type ID
   * @param destinationId destination ID
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto assignDestination(
      UUID program, UUID facilityType, UUID destinationId)
      throws InstantiationException, IllegalAccessException {

    permissionService.canManageStockDestinations();
    return doAssign(program, facilityType, destinationId,
        ERROR_DESTINATION_NOT_FOUND, ValidDestinationAssignment.class, validDestinationRepository);
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
    permissionService.canManageStockSources();
    return findAssignment(programId, facilityTypeId, sourceId, validSourceRepository);
  }

  /**
   * Find existing destination assignment.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @param destinationId  destination ID
   * @return a valid source destination dto
   */
  public ValidSourceDestinationDto findByProgramFacilityDestination(
      UUID programId, UUID facilityTypeId, UUID destinationId) {
    permissionService.canManageStockDestinations();
    return findAssignment(programId, facilityTypeId, destinationId, validDestinationRepository);
  }

  /**
   * Delete a source assignment by Id.
   *
   * @param assignmentId source assignment Id
   */
  public void deleteSourceAssignmentById(UUID assignmentId) {
    permissionService.canManageStockSources();
    doDelete(assignmentId, validSourceRepository, ERROR_SOURCE_ASSIGNMENT_NOT_FOUND);
  }

  /**
   * Delete a destination assignment by Id.
   *
   * @param assignmentId destination assignment Id
   */
  public void deleteDestinationAssignmentById(UUID assignmentId) {
    permissionService.canManageStockDestinations();
    doDelete(assignmentId, validDestinationRepository, ERROR_DESTINATION_ASSIGNMENT_NOT_FOUND);
  }

  private <T extends SourceDestinationAssignment> void doDelete(
      UUID assignmentId, SourceDestinationAssignmentRepository<T> repository, String errorKey) {
    if (!repository.exists(assignmentId)) {
      throw new ValidationMessageException(new Message(errorKey));
    }
    repository.delete(assignmentId);
  }

  private <T extends SourceDestinationAssignment> ValidSourceDestinationDto findAssignment(
      UUID programId, UUID facilityTypeId, UUID sourceId,
      SourceDestinationAssignmentRepository<T> repository) {

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    Node foundNode = nodeRepository.findByReferenceId(sourceId);
    if (foundNode != null) {
      T foundAssignment = repository.findByProgramIdAndFacilityTypeIdAndNodeId(
          programId, facilityTypeId, foundNode.getId());

      if (foundAssignment != null) {
        return createFrom(foundAssignment);
      }
    }

    return null;
  }

  private <T extends SourceDestinationAssignment> List<ValidSourceDestinationDto> findAssignments(
      UUID programId, UUID facilityTypeId,
      SourceDestinationAssignmentRepository<T> repository) {

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);

    List<T> assignments = repository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    return assignments.stream().map(this::createFrom).collect(Collectors.toList());
  }

  private <T extends SourceDestinationAssignment> ValidSourceDestinationDto doAssign(
      UUID program, UUID facilityType, UUID referenceId, String errorKey, Class<T> clazz,
      SourceDestinationAssignmentRepository<T> repository)
      throws IllegalAccessException, InstantiationException {

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(program, facilityType);

    if (facilityRefDataService.findOne(referenceId) != null) {
      return createFrom(createAssignment(
          program, facilityType, findOrCreateNode(referenceId, true), clazz, repository));
    } else if (organizationRepository.findOne(referenceId) != null) {
      return createFrom(createAssignment(
          program, facilityType, findOrCreateNode(referenceId, false), clazz, repository));
    }

    throw new ValidationMessageException(new Message(errorKey));
  }

  private <T extends SourceDestinationAssignment> T createAssignment(
      UUID program, UUID facilityType, Node node, Class<T> clazz,
      SourceDestinationAssignmentRepository<T> repository)
      throws IllegalAccessException, InstantiationException {

    T assignment = clazz.newInstance();
    assignment.setProgramId(program);
    assignment.setFacilityTypeId(facilityType);
    assignment.setNode(node);

    return repository.save(assignment);
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

  private ValidSourceDestinationDto createFrom(SourceDestinationAssignment assignment) {
    Node node = assignment.getNode();

    ValidSourceDestinationDto dto = createFrom(node);
    dto.setId(assignment.getId());
    dto.setNode(node);
    dto.setProgramId(assignment.getProgramId());
    dto.setFacilityTypeId(assignment.getFacilityTypeId());
    return dto;
  }

  private ValidSourceDestinationDto createFrom(Node node) {
    ValidSourceDestinationDto dto = new ValidSourceDestinationDto();

    boolean isRefDataFacility = node.isRefDataFacility();
    if (isRefDataFacility) {
      dto.setName(facilityRefDataService.findOne(node.getReferenceId()).getName());
    } else {
      dto.setName(organizationRepository.findOne(node.getReferenceId()).getName());
    }
    dto.setIsFreeTextAllowed(!isRefDataFacility);
    return dto;
  }
}
