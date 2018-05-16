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
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.ValidReasonAssignmentDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

public class ValidReasonAssignmentRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<ValidReasonAssignment> {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private ValidReasonAssignmentRepository repository;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  private static final UUID PROGRAM_ID = UUID.randomUUID();
  private static final UUID FACILITY_TYPE_ID = UUID.randomUUID();

  @Test(expected = PersistenceException.class)
  public void shouldThrowExceptionWhenSaveDuplicatedValidReason() throws Exception {
    ValidReasonAssignment validReasonAssignment = generateInstance();
    repository.save(validReasonAssignment);

    ValidReasonAssignment duplicateValidReason = new ValidReasonAssignment(
        PROGRAM_ID, FACILITY_TYPE_ID, false, validReasonAssignment.getReason());
    repository.save(duplicateValidReason);

    entityManager.flush();
  }

  @Test
  public void shouldReturnValidReasonWithProgramAndFacilityTypeAndReasonAndReasonTypes()
      throws Exception {
    ValidReasonAssignment validReasonAssignment = generateInstance();
    repository.save(validReasonAssignment);

    StockCardLineItemReason newReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .withName("Damage")
        .withDebitType()
        .build();
    reasonRepository.save(newReason);

    ValidReasonAssignment newAssignment = new ValidReasonAssignmentDataBuilder()
        .withProgram(PROGRAM_ID).withFacilityType(FACILITY_TYPE_ID).withReason(newReason).build();
    repository.save(newAssignment);

    List<ValidReasonAssignment> validReasonAssignments = repository.search(
            PROGRAM_ID, FACILITY_TYPE_ID, asSet(validReasonAssignment.getReason().getReasonType(),
            newReason.getReasonType()), newReason.getId());

    assertThat(validReasonAssignments.size(), is(1));
    assertThat(validReasonAssignments.get(0), is(newAssignment));
  }

  @Test
  public void shouldReturnValidReasonsIfNoneOfTheParamsIsSpecified() throws Exception {
    ValidReasonAssignment validReasonAssignment = generateInstance();
    repository.save(validReasonAssignment);

    StockCardLineItemReason newReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .withName("Damage")
        .withDebitType()
        .build();
    reasonRepository.save(newReason);

    ValidReasonAssignment newAssignment = new ValidReasonAssignment(
        PROGRAM_ID, FACILITY_TYPE_ID, false, newReason);
    repository.save(newAssignment);

    List<ValidReasonAssignment> validReasonAssignments = repository
        .search(null, null, null, null);

    assertThat(validReasonAssignments.size(), is(2));
    assertThat(validReasonAssignments, hasItems(validReasonAssignment, newAssignment));
  }

  @Override
  CrudRepository<ValidReasonAssignment, UUID> getRepository() {
    return repository;
  }

  @Override
  ValidReasonAssignment generateInstance() throws Exception {
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
