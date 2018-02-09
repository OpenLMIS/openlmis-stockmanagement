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
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.reason.ValidReasonAssignment;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.ValidReasonAssignmentDataBuilder;

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
        programId, facilityTypeId, Arrays.asList("CREDIT", "DEBIT"));

    assertThat(assignmentList.size(), is(1));
    assertThat(assignmentList.get(0), is(validReasonAssignment));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationMessageExceptionWhenReasonTypeParamIsNull()
      throws ValidationMessageException {

    when(stockCardLineItemReasonRepository.findByReasonTypeIn(Arrays.asList(
        ReasonType.CREDIT, ReasonType.DEBIT))).thenReturn(Collections.singletonList(newReason));

    when(validReasonAssignmentRepository.findByProgramIdAndFacilityTypeIdAndReasonIn(eq(programId),
        eq(facilityTypeId), eq(Collections.singletonList(newReason))))
        .thenReturn(Collections.singletonList(validReasonAssignment));

    validReasonAssignmentService.search(programId, facilityTypeId, Arrays.asList("CREDIT", null));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationMessageExceptionWhenReasonTypeParamIsInvalid()
      throws ValidationMessageException {

    when(stockCardLineItemReasonRepository.findByReasonTypeIn(Arrays.asList(
        ReasonType.CREDIT, ReasonType.DEBIT))).thenReturn(Collections.singletonList(newReason));

    when(validReasonAssignmentRepository.findByProgramIdAndFacilityTypeIdAndReasonIn(eq(programId),
        eq(facilityTypeId), eq(Collections.singletonList(newReason))))
        .thenReturn(Collections.singletonList(validReasonAssignment));

    validReasonAssignmentService.search(programId, facilityTypeId,
        Collections.singletonList("INVALID"));
  }

  @Test
  public void shouldReturnValidReasonAssignmentsIfReasonTypeParamIsNotSpecified() {

    when(validReasonAssignmentRepository.findByProgramIdAndFacilityTypeId(
        programId, facilityTypeId)).thenReturn(Collections.singletonList(validReasonAssignment));

    List<ValidReasonAssignment> assignmentList = validReasonAssignmentService.search(
        programId, facilityTypeId, null);

    assertThat(assignmentList.size(), is(1));
    assertThat(assignmentList.get(0), is(validReasonAssignment));
  }

  @Test
  public void shouldReturnValidReasonAssignmentsIfStockCardLineItemsListIsEmpty() {

    when(stockCardLineItemReasonRepository.findByReasonTypeIn(Collections.singletonList(
        ReasonType.DEBIT))).thenReturn(Collections.emptyList());

    List<ValidReasonAssignment> assignmentList = validReasonAssignmentService.search(
        programId, facilityTypeId, Collections.singletonList("DEBIT"));

    assertThat(assignmentList.size(), is(0));
  }

  private ValidReasonAssignment generateValidReasonAssignment(StockCardLineItemReason reason) {
    return new ValidReasonAssignmentDataBuilder()
        .withReason(reason)
        .withFacilityType(facilityTypeId)
        .withProgram(programId)
        .build();
  }

  private StockCardLineItemReason generateStockCardLineItemReason() {
    return new StockCardLineItemReasonDataBuilder()
        .withName("Name")
        .withDescription("Description")
        .withDebitType()
        .build();
  }

}
