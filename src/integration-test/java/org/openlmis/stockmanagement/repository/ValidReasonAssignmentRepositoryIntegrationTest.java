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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
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
  public void shouldReturnValidReasonWithProgramAndFacilityTypeAndReasonTypes() throws Exception {
    ValidReasonAssignment validReasonAssignment = generateInstance();
    repository.save(validReasonAssignment);

    StockCardLineItemReason newReason = StockCardLineItemReason
        .builder()
        .name("Name")
        .description("Description")
        .isFreeTextAllowed(true)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.DEBIT)
        .build();
    reasonRepository.save(newReason);

    ValidReasonAssignment newAssignment = new ValidReasonAssignment(
        PROGRAM_ID, FACILITY_TYPE_ID, false, newReason);
    repository.save(newAssignment);

    List<StockCardLineItemReason> reasons = Arrays.asList(validReasonAssignment.getReason(),
        newReason);

    List<ValidReasonAssignment> validReasonAssignments = repository
        .findByProgramIdAndFacilityTypeIdAndReasonIn(PROGRAM_ID, FACILITY_TYPE_ID, reasons);

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
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name("Name" + instanceNumber)
        .description("Description" + instanceNumber)
        .isFreeTextAllowed(instanceNumber % 2 == 0)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.CREDIT)
        .build();
    reasonRepository.save(reason);
    return new ValidReasonAssignment(PROGRAM_ID, FACILITY_TYPE_ID, false, reason);
  }
}
