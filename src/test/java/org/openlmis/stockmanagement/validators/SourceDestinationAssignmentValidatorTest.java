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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_IN_VALID_LIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_IN_VALID_LIST;

@RunWith(MockitoJUnitRunner.class)
public class SourceDestinationAssignmentValidatorTest {

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ValidSourceAssignmentRepository validSourceAssignmentRepository;

  @Mock
  private ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @InjectMocks
  private SourceDestinationAssignmentValidator sourceDestinationAssignmentValidator;

  @Test
  public void should_not_pass_when_event_has_both_source_and_destination() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();
    eventDto.setSourceId(UUID.randomUUID());
    eventDto.setDestinationId(UUID.randomUUID());

    //when
    try {
      sourceDestinationAssignmentValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(
              ERROR_SOURCE_DESTINATION_BOTH_PRESENT));
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_not_pass_when_event_has_source_not_in_valid_list() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();
    eventDto.setSourceId(UUID.randomUUID());
    eventDto.setDestinationId(null);

    mockFacilityRefDataService(eventDto);
    when(validSourceAssignmentRepository
            .findByProgramIdAndFacilityTypeId(any(UUID.class), any(UUID.class)))
            .thenReturn(new ArrayList<>());

    //when
    try {
      sourceDestinationAssignmentValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(ERROR_SOURCE_NOT_IN_VALID_LIST));
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_not_pass_when_event_has_destination_not_in_valid_list() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();
    eventDto.setDestinationId(UUID.randomUUID());
    eventDto.setSourceId(null);

    mockFacilityRefDataService(eventDto);
    when(validDestinationAssignmentRepository
            .findByProgramIdAndFacilityTypeId(any(UUID.class), any(UUID.class)))
            .thenReturn(new ArrayList<>());

    //when
    try {
      sourceDestinationAssignmentValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(ERROR_DESTINATION_NOT_IN_VALID_LIST));
      return;
    }

    Assert.fail();
  }

  private void mockFacilityRefDataService(StockEventDto eventDto) {
    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(UUID.randomUUID());
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setType(facilityTypeDto);
    when(facilityReferenceDataService.findOne(eventDto.getFacilityId())).thenReturn(facilityDto);
  }
}