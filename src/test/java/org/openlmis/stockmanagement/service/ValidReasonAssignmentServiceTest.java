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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;

@RunWith(MockitoJUnitRunner.class)
public class ValidReasonAssignmentServiceTest {

  @Mock
  private ValidReasonAssignmentRepository validReasonAssignmentRepository;

  @Mock
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @InjectMocks
  private ValidReasonAssignmentService validReasonAssignmentService;

  private UUID facilityTypeId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();

  private StockCardLineItemReason newReason;
  private ValidReasonAssignment validReasonAssignment;

  @Before
  public void setUp() {
    newReason = generateStockCardLineItemReason();
    validReasonAssignment = generateValidReasonAssignment(newReason);
  }

  @Test
  public void shouldReturnValidReasonAssignments() {

    when(stockCardLineItemReasonRepository.findByReasonTypeIn(Arrays.asList(
        ReasonType.CREDIT, ReasonType.DEBIT))).thenReturn(Collections.singletonList(newReason));

    when(validReasonAssignmentRepository.findByProgramIdAndFacilityTypeIdAndReasonIn(eq(programId),
        eq(facilityTypeId), eq(Collections.singletonList(newReason))))
        .thenReturn(Collections.singletonList(validReasonAssignment));

    List<ValidReasonAssignment> assignmentList = validReasonAssignmentService.search(
        programId, facilityTypeId, Arrays.asList(ReasonType.CREDIT, ReasonType.DEBIT));

    assertThat(assignmentList.size(), is(1));
    assertThat(assignmentList.get(0), is(validReasonAssignment));
  }

  @Test
  public void shouldReturnValidReasonAssignmentsIfReasonTypeParamIsNull() {

    when(stockCardLineItemReasonRepository.findByReasonTypeIn(Arrays.asList(
        ReasonType.BALANCE_ADJUSTMENT, ReasonType.DEBIT))).thenReturn(Collections.emptyList());

    when(validReasonAssignmentRepository.findByProgramIdAndFacilityTypeId(
        programId, facilityTypeId)).thenReturn(Collections.singletonList(validReasonAssignment));

    List<ValidReasonAssignment> assignmentList = validReasonAssignmentService.search(
        programId, facilityTypeId, null);

    assertThat(assignmentList.size(), is(1));
    assertThat(assignmentList.get(0), is(validReasonAssignment));
  }

  @Test
  public void shouldReturnValidReasonAssignmentsIfNoStockCardLineItemsListIsEmpty() {

    when(stockCardLineItemReasonRepository.findByReasonTypeIn(null))
        .thenReturn(Collections.singletonList(newReason));

    when(validReasonAssignmentRepository.findByProgramIdAndFacilityTypeId(
        programId, facilityTypeId)).thenReturn(Collections.singletonList(validReasonAssignment));

    List<ValidReasonAssignment> assignmentList = validReasonAssignmentService.search(
        programId, facilityTypeId, null);

    assertThat(assignmentList.size(), is(1));
    assertThat(assignmentList.get(0), is(validReasonAssignment));
  }

  private ValidReasonAssignment generateValidReasonAssignment(StockCardLineItemReason reason) {
    ValidReasonAssignment validReasonAssignment = ValidReasonAssignment
        .builder()
        .reason(reason)
        .facilityTypeId(facilityTypeId)
        .programId(programId)
        .hidden(false)
        .build();
    validReasonAssignmentRepository.save(validReasonAssignment);

    return validReasonAssignment;
  }

  private StockCardLineItemReason generateStockCardLineItemReason() {
    StockCardLineItemReason newReason = StockCardLineItemReason
        .builder()
        .name("Name")
        .description("Description")
        .isFreeTextAllowed(true)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.DEBIT)
        .build();
    stockCardLineItemReasonRepository.save(newReason);

    return newReason;
  }

}
