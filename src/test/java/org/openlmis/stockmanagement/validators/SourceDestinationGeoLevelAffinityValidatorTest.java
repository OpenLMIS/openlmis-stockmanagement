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

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.ValidSourceDestinationDtoDataBuilder.createValidSourceDestinationDto;

import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.service.ValidDestinationService;
import org.openlmis.stockmanagement.service.ValidSourceService;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class SourceDestinationGeoLevelAffinityValidatorTest extends BaseValidatorTest {

  private static final String ORGANIZATION_NODE_NAME = "ORGANIZATION_NODE_NAME";
  private static final String FACILITY_NODE_NAME = "FACILITY_NODE_NAME";
  private static final String CONTEXT_FACILITY_NAME = "CONTEXT_FACILITY";

  @Mock
  private ValidDestinationService validDestinationService;

  @Mock
  private ValidSourceService validSourceService;

  @InjectMocks
  private SourceDestinationGeoLevelAffinityValidator sourceDestinationGeoLeveLAffinityValidator;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void shouldNotRejectSourceWhenGeoAffinityMatch() {
    UUID sourceId = UUID.randomUUID();
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createWithSourceAndDestination(sourceId, null);

    List<ValidSourceDestinationDto> validDestinationAssignments = asList(
        createValidSourceDestinationDto(sourceId, FACILITY_NODE_NAME),
        createValidSourceDestinationDto(randomUUID(), ORGANIZATION_NODE_NAME));

    when(
        validSourceService.findSources(stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(), null, Pageable.unpaged()))
        .thenReturn(Pagination.getPage(validDestinationAssignments));

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotRejectDestinationWhenGeoAffinityMatch() {
    UUID destinationId = UUID.randomUUID();
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createWithSourceAndDestination(null, destinationId);

    List<ValidSourceDestinationDto> validDestinationAssignments = asList(
        createValidSourceDestinationDto(destinationId, FACILITY_NODE_NAME),
        createValidSourceDestinationDto(randomUUID(), ORGANIZATION_NODE_NAME));

    when(
        validDestinationService.findDestinations(
                stockEventDto.getProgramId(), stockEventDto.getFacilityId(),
            null, Pageable.unpaged()))
        .thenReturn(Pagination.getPage(validDestinationAssignments));

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldRejectSourceWhenGeoAffinityNotMatch() {
    UUID sourceId = UUID.randomUUID();
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createWithSourceAndDestination(sourceId, null);

    FacilityDto facility = FacilityDto.builder().name(CONTEXT_FACILITY_NAME).build();
    when(facilityService.findOne(any(UUID.class))).thenReturn(facility);
    setContext(stockEventDto);

    List<ValidSourceDestinationDto> validDestinationAssignments = asList(
        createValidSourceDestinationDto(randomUUID(), FACILITY_NODE_NAME),
        createValidSourceDestinationDto(randomUUID(), ORGANIZATION_NODE_NAME));

    when(
        validSourceService.findSources(stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(), null, Pageable.unpaged()))
        .thenReturn(Pagination.getPage(validDestinationAssignments));

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(MessageKeys.ERROR_SOURCE_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY);

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldRejectSourceWhenValidDestinationAssignmentsIsEmpty() {
    UUID sourceId = UUID.randomUUID();
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createWithSourceAndDestination(sourceId, null);

    FacilityDto facility = FacilityDto.builder().name(CONTEXT_FACILITY_NAME).build();

    when(facilityService.findOne(any(UUID.class))).thenReturn(facility);
    setContext(stockEventDto);

    when(
        validSourceService.findSources(stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(), null, Pageable.unpaged()))
        .thenReturn(Page.empty());

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(MessageKeys.ERROR_SOURCE_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY);

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldRejectSourceWithMessageWhenGeoAffinityNotMatch() {
    UUID sourceId = UUID.randomUUID();
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createWithSourceAndDestination(sourceId, null);
    FacilityDto facility = FacilityDto.builder().name(CONTEXT_FACILITY_NAME).build();
    when(facilityService.findOne(any(UUID.class))).thenReturn(facility);
    setContext(stockEventDto);

    List<ValidSourceDestinationDto> validDestinationAssignments = asList(
        createValidSourceDestinationDto(randomUUID(), FACILITY_NODE_NAME),
        createValidSourceDestinationDto(randomUUID(), ORGANIZATION_NODE_NAME));

    when(validSourceService.findSources(stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(),  null, Pageable.unpaged()))
        .thenReturn(Pagination.getPage(validDestinationAssignments));

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(MessageKeys.ERROR_SOURCE_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY);

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldRejectDestinationWhenGeoAffinityNotMatch() {
    UUID destinationId = UUID.randomUUID();
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createWithSourceAndDestination(null, destinationId);

    FacilityDto facility = FacilityDto.builder().name(CONTEXT_FACILITY_NAME).build();
    when(facilityService.findOne(any(UUID.class))).thenReturn(facility);
    setContext(stockEventDto);

    List<ValidSourceDestinationDto> validDestinationAssignments = asList(
        createValidSourceDestinationDto(randomUUID(), FACILITY_NODE_NAME),
        createValidSourceDestinationDto(randomUUID(), ORGANIZATION_NODE_NAME));

    when(validDestinationService.findDestinations(stockEventDto.getProgramId(),
          stockEventDto.getFacilityId(), null, Pageable.unpaged()))
            .thenReturn(Pagination.getPage(validDestinationAssignments));

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
            MessageKeys.ERROR_DESTINATION_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY);

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldSkipValidationIfPhysicalInventory() {
    StockEventDto stockEventDto = StockEventDtoDataBuilder
        .createNoSourceDestinationStockEventDto();

    stockEventDto.getLineItems().forEach(lineItem -> lineItem.setReasonId(null));

    sourceDestinationGeoLeveLAffinityValidator.validate(stockEventDto);

    verify(validDestinationService, times(0))
            .findDestinations(any(), any(), any(), any());
    verify(validSourceService, times(0))
            .findSources(any(), any(), any(), any());
  }
}
