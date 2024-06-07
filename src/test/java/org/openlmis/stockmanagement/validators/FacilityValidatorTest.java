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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_MUST_BE_WARD_SERVICE_OF_FACILITY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NODE_NOT_FOUND;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventLineItem;

import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.referencedata.GeographicZoneDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.GeographicZoneDtoDataBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FacilityValidatorTest extends BaseValidatorTest {

  public static final String FACILITY_TYPE_CODE = "TEST";
  public static final String WARD_SERVICE_TYPE_CODE = "WS";

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private Node node;

  @InjectMocks
  private FacilityValidator facilityValidator;

  private GeographicZoneDto geographicZone;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    when(node.getReferenceId()).thenReturn(UUID.randomUUID());
    when(node.isRefDataFacility()).thenReturn(true);

    geographicZone = new GeographicZoneDtoDataBuilder().withId(UUID.randomUUID()).build();
  }

  @Test
  public void shouldPassIfEventLineItemsListIsEmpty() {
    //given
    StockEventDto eventDto = createStockEventDto();
    eventDto.setLineItems(emptyList());

    //when
    facilityValidator.validate(eventDto);

    //then: no error
  }

  @Test
  public void shouldPassIfEventIsPhysicalInventory() {
    //given
    StockEventDto eventDto = mock(StockEventDto.class);
    when(eventDto.isPhysicalInventory()).thenReturn(true);

    //when
    facilityValidator.validate(eventDto);

    //then: no error
  }

  @Test
  public void shouldPassIfEventLineItemDestinationIsNotWardServiceType() {
    //given
    StockEventDto eventDto = createStockEventDto();
    FacilityDto eventFacility = generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);

    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(eventFacility);
    when(nodeRepository.findById(eventDto.getLineItems().get(0).getDestinationId()))
        .thenReturn(Optional.of(node));

    FacilityDto destinationFacility =
        generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);
    when(facilityService.findOne(node.getReferenceId())).thenReturn(destinationFacility);

    //when
    facilityValidator.validate(eventDto);

    //then: no error
  }

  @Test
  public void shouldNotPassIfEventLineItemDestinationIsWardServiceType() {
    //given
    StockEventDto eventDto = createStockEventDto();
    FacilityDto eventFacility = generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);

    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(eventFacility);
    when(nodeRepository.findById(eventDto.getLineItems().get(0).getDestinationId()))
        .thenReturn(Optional.of(node));

    GeographicZoneDto geographicZone2 = new GeographicZoneDtoDataBuilder()
        .withId(UUID.randomUUID())
        .build();
    FacilityDto destinationFacility =
        generateFacility(eventDto, WARD_SERVICE_TYPE_CODE, geographicZone2);
    when(facilityService.findOne(node.getReferenceId())).thenReturn(destinationFacility);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        containsString(ERROR_DESTINATION_MUST_BE_WARD_SERVICE_OF_FACILITY));

    //when
    facilityValidator.validate(eventDto);
  }

  @Test
  public void shouldPassIfEventLineItemDestinationIsWardServiceTypeInSameZoneAsEventFacility() {
    //given
    StockEventDto eventDto = createStockEventDto();
    FacilityDto eventFacility = generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);

    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(eventFacility);
    when(nodeRepository.findById(eventDto.getLineItems().get(0).getDestinationId()))
        .thenReturn(Optional.of(node));

    FacilityDto destinationFacility =
        generateFacility(eventDto, WARD_SERVICE_TYPE_CODE, geographicZone);
    when(facilityService.findOne(node.getReferenceId())).thenReturn(destinationFacility);

    //when
    facilityValidator.validate(eventDto);

    //then: no error
  }

  @Test
  public void shouldPassIfEventLineItemDestinationIsNull() {
    //given
    StockEventDto eventDto = createStockEventDto();
    FacilityDto eventFacility = generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);

    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(eventFacility);
    when(nodeRepository.findById(eventDto.getLineItems().get(0).getDestinationId()))
        .thenReturn(Optional.of(node));
    when(node.isRefDataFacility()).thenReturn(false);

    //when
    facilityValidator.validate(eventDto);

    //then: no error
  }

  @Test
  public void shouldPassIfNodeRefDataIsNotFacility() {
    //given
    StockEventDto eventDto = createStockEventDto();
    StockEventLineItemDto lineItem = createStockEventLineItem();
    lineItem.setDestinationId(null);
    eventDto.setLineItems(singletonList(lineItem));
    FacilityDto eventFacility = generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);
    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(eventFacility);

    //when
    facilityValidator.validate(eventDto);

    //then: no error
  }

  @Test
  public void shouldNotPassIfNodeNotFound() {
    //given
    StockEventDto eventDto = createStockEventDto();
    FacilityDto eventFacility = generateFacility(eventDto, FACILITY_TYPE_CODE, geographicZone);

    when(facilityService.findOne(eventDto.getFacilityId())).thenReturn(eventFacility);
    when(nodeRepository.findById(eventDto.getLineItems().get(0).getDestinationId()))
        .thenReturn(Optional.empty());

    expectedException.expect(ResourceNotFoundException.class);
    expectedException.expectMessage(containsString(ERROR_NODE_NOT_FOUND));

    //when
    facilityValidator.validate(eventDto);
  }

  private FacilityDto generateFacility(StockEventDto eventDto, String facilityTypeCode,
      GeographicZoneDto geographicZone) {
    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(UUID.randomUUID());
    facilityTypeDto.setCode(facilityTypeCode);
    FacilityDto facility = new FacilityDto();
    facility.setId(eventDto.getFacilityId());
    facility.setType(facilityTypeDto);
    facility.setGeographicZone(geographicZone);

    setContext(eventDto);
    return facility;
  }

}
