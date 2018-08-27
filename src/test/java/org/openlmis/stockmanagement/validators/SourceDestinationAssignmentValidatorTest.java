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

import static java.util.Collections.singletonList;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_IN_VALID_LIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_IN_VALID_LIST;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;

import java.util.ArrayList;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityTypeDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;

@RunWith(MockitoJUnitRunner.class)
public class SourceDestinationAssignmentValidatorTest extends BaseValidatorTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @InjectMocks
  private SourceDestinationAssignmentValidator sourceDestinationAssignmentValidator;

  @Mock
  private ValidSourceAssignment validSourceAssignment;

  @Mock
  private ValidDestinationAssignment validDestinationAssignment;

  @Mock
  private Node node;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    when(validSourceAssignment.getNode()).thenReturn(node);
    when(validDestinationAssignment.getNode()).thenReturn(node);
    when(node.getId()).thenReturn(UUID.randomUUID());

    when(validSourceAssignmentRepository
        .findByProgramIdAndFacilityTypeId(any(UUID.class), any(UUID.class)))
        .thenReturn(singletonList(validSourceAssignment));

    when(validDestinationAssignmentRepository
        .findByProgramIdAndFacilityTypeId(any(UUID.class), any(UUID.class)))
        .thenReturn(singletonList(validDestinationAssignment));
  }

  @Test
  public void shouldNotPassWhenEventHasBothSourceAndDestination() throws Exception {
    //given
    StockEventDto eventDto = createStockEventDto();
    createContextWithFacility(eventDto);

    eventDto.getLineItems().get(0).setSourceId(UUID.randomUUID());
    eventDto.getLineItems().get(0).setDestinationId(UUID.randomUUID());

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(containsString(ERROR_SOURCE_DESTINATION_BOTH_PRESENT));

    //when
    sourceDestinationAssignmentValidator.validate(eventDto);
  }

  @Test
  public void shouldNotPassWhenEventHasSourceNotInValidList() throws Exception {
    //given
    StockEventDto eventDto = createStockEventDto();
    createContextWithFacility(eventDto);

    eventDto.getLineItems().get(0).setSourceId(UUID.randomUUID());
    eventDto.getLineItems().get(0).setDestinationId(null);

    when(validSourceAssignmentRepository
        .findByProgramIdAndFacilityTypeId(any(UUID.class), any(UUID.class)))
        .thenReturn(new ArrayList<>());

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(containsString(ERROR_SOURCE_NOT_IN_VALID_LIST));

    //when
    sourceDestinationAssignmentValidator.validate(eventDto);
  }

  @Test
  public void shouldNotPassWhenEventHasDestinationNotInValidList() throws Exception {
    //given
    StockEventDto eventDto = createStockEventDto();
    createContextWithFacility(eventDto);

    eventDto.getLineItems().get(0).setDestinationId(UUID.randomUUID());
    eventDto.getLineItems().get(0).setSourceId(null);

    when(validDestinationAssignmentRepository
        .findByProgramIdAndFacilityTypeId(any(UUID.class), any(UUID.class)))
        .thenReturn(new ArrayList<>());

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(containsString(ERROR_DESTINATION_NOT_IN_VALID_LIST));

    //when
    sourceDestinationAssignmentValidator.validate(eventDto);
  }

  @Test
  //this validator does not care if program missing
  //that is handled in other validators
  public void shouldPassIfProgramMissing() throws Exception {
    //given
    StockEventDto eventDto = createStockEventDto();
    createContextWithFacility(eventDto);

    eventDto.setProgramId(null);
    eventDto.getLineItems().get(0).setDestinationId(node.getId());
    eventDto.getLineItems().get(0).setSourceId(null);

    //when
    sourceDestinationAssignmentValidator.validate(eventDto);

    //then: no error
  }

  @Test
  //this validator does not care if facility type not found in ref data
  //that is handled in other validators
  public void shouldPassIfFacilityNotFoundInRefData() throws Exception {
    //given
    StockEventDto eventDto = createStockEventDto();
    createContextWithFacility(eventDto);

    eventDto.setFacilityId(null);
    eventDto.getLineItems().get(0).setDestinationId(node.getId());
    eventDto.getLineItems().get(0).setSourceId(null);

    //when
    sourceDestinationAssignmentValidator.validate(eventDto);

    //then: no error
  }

  private void createContextWithFacility(StockEventDto eventDto) {
    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(UUID.randomUUID());
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setId(eventDto.getFacilityId());
    facilityDto.setType(facilityTypeDto);

    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(facilityDto);

    setContext(eventDto);
  }
}