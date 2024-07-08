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

package org.openlmis.stockmanagement.repository.custom.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.repository.custom.impl.ValidReasonAssignmentRepositoryImpl.FACILITY_TYPE_ID;
import static org.openlmis.stockmanagement.repository.custom.impl.ValidReasonAssignmentRepositoryImpl.PROGRAM_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;

@RunWith(MockitoJUnitRunner.class)
public class ValidReasonAssignmentRepositoryImplTest {

  @InjectMocks
  private ValidReasonAssignmentRepositoryImpl repository;

  @Mock
  private EntityManager entityManager;

  @Test
  public void shouldSearchForProgramIdsOnly() {
    //given
    UUID programId1 = UUID.randomUUID();
    UUID programId2 = UUID.randomUUID();
    Collection<UUID> programIds = new ArrayList<>();
    programIds.add(programId1);
    programIds.add(programId2);

    List<ValidReasonAssignment> validReasonAssignmentList = mock(List.class);
    TypedQuery typedQuery = mock(TypedQuery.class);
    when(typedQuery.getResultList())
        .thenReturn(validReasonAssignmentList);

    CriteriaQuery query = mock(CriteriaQuery.class);
    Predicate conjunctionPredicate = mock(Predicate.class);

    Predicate inPredicate = mock(Predicate.class);
    Path programIdPath = mock(Path.class);
    when(programIdPath.in(programIds)).thenReturn(inPredicate);

    Root root = mock(Root.class);
    when(root.get(PROGRAM_ID)).thenReturn(programIdPath);
    Predicate wherePredicate = mock(Predicate.class);

    CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
    when(criteriaBuilder.createQuery(ValidReasonAssignment.class))
        .thenReturn(query);
    when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
    when(criteriaBuilder.and(conjunctionPredicate, inPredicate)).thenReturn(wherePredicate);

    when(query.from(ValidReasonAssignment.class)).thenReturn(root);

    when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    when(entityManager.createQuery(query))
        .thenReturn(typedQuery);

    //when
    List<ValidReasonAssignment> searchResults =
        repository.search(programIds, null, null, null);

    //then
    verify(entityManager).createQuery(query);
    verify(query).where(wherePredicate);
    assertThat(searchResults).isEqualTo(validReasonAssignmentList);
  }

  @Test
  public void shouldSearchForFacilityTypeIdOnly() {
    //given
    UUID facilityTypeId = UUID.randomUUID();

    List<ValidReasonAssignment> validReasonAssignmentList = mock(List.class);
    TypedQuery typedQuery = mock(TypedQuery.class);
    when(typedQuery.getResultList())
        .thenReturn(validReasonAssignmentList);

    CriteriaQuery query = mock(CriteriaQuery.class);
    Predicate conjunctionPredicate = mock(Predicate.class);

    Predicate equalPredicate = mock(Predicate.class);
    Path facilityTypeIdPath = mock(Path.class);

    Root root = mock(Root.class);
    when(root.get(FACILITY_TYPE_ID)).thenReturn(facilityTypeIdPath);
    Predicate wherePredicate = mock(Predicate.class);

    CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
    when(criteriaBuilder.createQuery(ValidReasonAssignment.class))
        .thenReturn(query);
    when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
    when(criteriaBuilder.equal(facilityTypeIdPath, facilityTypeId)).thenReturn(equalPredicate);
    when(criteriaBuilder.and(conjunctionPredicate, equalPredicate)).thenReturn(wherePredicate);

    when(query.from(ValidReasonAssignment.class)).thenReturn(root);

    when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    when(entityManager.createQuery(query))
        .thenReturn(typedQuery);

    //when
    List<ValidReasonAssignment> searchResults =
        repository.search(null, facilityTypeId, null, null);

    //then
    verify(entityManager).createQuery(query);
    verify(query).where(wherePredicate);
    assertThat(searchResults).isEqualTo(validReasonAssignmentList);
  }
}
