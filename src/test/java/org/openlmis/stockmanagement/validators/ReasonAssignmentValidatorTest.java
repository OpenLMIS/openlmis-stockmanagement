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

package org.openlmis.stockmanagement.validators;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_REASON_NOT_IN_VALID_LIST;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidReasonAssignmentRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.util.ArrayList;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ReasonAssignmentValidatorTest {
  @Rule
  public ExpectedException expectedEx = none();

  @Mock
  private ValidReasonAssignmentRepository validReasonAssignmentRepository;

  @InjectMocks
  private ReasonAssignmentValidator reasonAssignmentValidator;

  @Test
  public void should_not_throw_error_if_event_has_no_reason_id() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setReasonId(null);
    stockEventDto.setContext(StockEventProcessContext.builder()
        .facility(createFacilityDto()).build());
    //when
    reasonAssignmentValidator.validate(stockEventDto);

    //then: no exception
  }

  @Test
  public void should_throw_error_if_event_reason_id_not_found_in_assignment_list()
      throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_EVENT_REASON_NOT_IN_VALID_LIST);

    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setReasonId(UUID.randomUUID());

    FacilityDto facilityDto = createFacilityDto();

    UUID programId = stockEventDto.getProgramId();

    stockEventDto.setContext(StockEventProcessContext.builder().facility(facilityDto).build());
    when(validReasonAssignmentRepository
        .findByProgramIdAndFacilityTypeId(programId, facilityDto.getType().getId()))
        .thenReturn(new ArrayList<>());

    //when
    reasonAssignmentValidator.validate(stockEventDto);
  }

  private FacilityDto createFacilityDto() {
    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(UUID.randomUUID());

    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setType(facilityTypeDto);
    return facilityDto;
  }

  @Test
  public void should_not_throw_error_if_event_has_facility_id_not_in_ref_data()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setContext(new StockEventProcessContext());

    //when
    reasonAssignmentValidator.validate(stockEventDto);

    //then: no error
  }

  @Test
  public void should_not_throw_error_if_event_has_no_program_id()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setContext(new StockEventProcessContext());
    stockEventDto.setProgramId(null);

    //when
    reasonAssignmentValidator.validate(stockEventDto);

    //then: no error
  }
}