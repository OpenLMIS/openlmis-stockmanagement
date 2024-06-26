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

package org.openlmis.stockmanagement.repository;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.ValidReasonAssignmentDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class ValidReasonAssignmentRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<ValidReasonAssignment> {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private ValidReasonAssignmentRepository repository;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  private ValidReasonAssignment validReasonAssignment;
  private ValidReasonAssignment secondAssignment;
  private StockCardLineItemReason stockCardLineItemReason;

  private static final UUID PROGRAM_ID = UUID.randomUUID();
  private static final UUID FACILITY_TYPE_ID = UUID.randomUUID();

  @Before
  public void setUp() {
    validReasonAssignment = generateInstance();
    repository.save(validReasonAssignment);

    stockCardLineItemReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .withName("Damage")
        .withDebitType()
        .build();
    reasonRepository.save(stockCardLineItemReason);

    secondAssignment = new ValidReasonAssignmentDataBuilder()
        .withProgram(UUID.randomUUID())
        .withFacilityType(UUID.randomUUID())
        .withReason(stockCardLineItemReason)
        .build();
    repository.save(secondAssignment);
  }

  @Test(expected = PersistenceException.class)
  public void shouldThrowExceptionWhenSaveDuplicatedValidReason() {
    ValidReasonAssignment duplicateValidReason = new ValidReasonAssignment(
        PROGRAM_ID, FACILITY_TYPE_ID, false, validReasonAssignment.getReason());
    repository.save(duplicateValidReason);

    entityManager.flush();
  }

  @Test
  public void shouldReturnValidReasonWithProgramAndFacilityTypeAndReasonAndReasonTypes() {
    ValidReasonAssignment newAssignment = new ValidReasonAssignmentDataBuilder()
        .withProgram(PROGRAM_ID)
        .withFacilityType(FACILITY_TYPE_ID)
        .withReason(stockCardLineItemReason)
        .build();
    repository.save(newAssignment);

    List<ValidReasonAssignment> validReasonAssignments = repository.search(
        Collections.singleton(PROGRAM_ID), FACILITY_TYPE_ID, Sets.newHashSet(
            validReasonAssignment.getReason().getReasonType(),
            stockCardLineItemReason.getReasonType()),
        stockCardLineItemReason.getId());

    assertThat(validReasonAssignments.size(), is(1));
    assertThat(validReasonAssignments.get(0), is(newAssignment));
  }

  @Test
  public void shouldReturnValidReasonAssignmentsIfStockCardLineItemsListIsEmpty() {
    List<ValidReasonAssignment> assignmentList = repository.search(
        null, null, Sets.newHashSet(ReasonType.BALANCE_ADJUSTMENT), null);

    assertThat(assignmentList.size(), is(0));
  }

  @Test
  public void shouldReturnValidReasonsIfNoneOfTheParamsIsSpecified() {
    ValidReasonAssignment newAssignment = new ValidReasonAssignment(
        PROGRAM_ID, FACILITY_TYPE_ID, false, stockCardLineItemReason);
    repository.save(newAssignment);

    List<ValidReasonAssignment> validReasonAssignments = repository
        .search(null, null, null, null);

    assertThat(validReasonAssignments.size(), is(3));
    assertThat(validReasonAssignments, hasItems(
        validReasonAssignment, newAssignment, secondAssignment));
  }

  @Override
  CrudRepository<ValidReasonAssignment, UUID> getRepository() {
    return repository;
  }

  @Override
  ValidReasonAssignment generateInstance() {
    int instanceNumber = getNextInstanceNumber();
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .withName("Name" + instanceNumber)
        .withDebitType()
        .build();
    reasonRepository.save(reason);
    return new ValidReasonAssignment(PROGRAM_ID, FACILITY_TYPE_ID, false, reason);
  }
}
