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
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_NOT_VALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_NOT_VALID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SourceDestinationValidatorTest extends BaseTest {

  @Autowired
  private SourceDestinationValidator sourceDestinationValidator;

  @MockBean
  private FacilityReferenceDataService facilityReferenceDataService;

  @Test
  public void should_not_pass_when_event_has_both_source_and_destination() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();
    eventDto.setSourceId(UUID.randomUUID());
    eventDto.setDestinationId(UUID.randomUUID());

    //when
    try {
      sourceDestinationValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(
              ERROR_SOURCE_DESTINATION_BOTH_PRESENT));
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_not_pass_when_event_has_source_that_is_not_a_node() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();
    eventDto.setSourceId(UUID.randomUUID());//this random id is not a node
    eventDto.setDestinationId(null);

    mockFacilityRefDataService(eventDto);

    //when
    try {
      sourceDestinationValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(ERROR_SOURCE_NOT_VALID));
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_not_pass_when_event_has_source_not_in_valid_list() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();

    eventDto.setFacilityId(UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060"));
    eventDto.setProgramId(UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3"));
    //the following source id is not in valid source list
    eventDto.setSourceId(UUID.fromString("e89eaf68-50c1-47f2-b83a-5b51ffa2206e"));
    eventDto.setDestinationId(null);

    mockFacilityRefDataService(eventDto);

    //when
    try {
      sourceDestinationValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(ERROR_SOURCE_NOT_VALID));
      return;
    }

    Assert.fail();
  }


  @Test
  public void should_not_pass_when_event_has_destination_that_is_not_a_node() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();
    eventDto.setDestinationId(UUID.randomUUID());//this random id is not a node
    eventDto.setSourceId(null);

    mockFacilityRefDataService(eventDto);

    //when
    try {
      sourceDestinationValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(ERROR_DESTINATION_NOT_VALID));
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_not_pass_when_event_has_destination_not_in_valid_list() throws Exception {
    //given
    StockEventDto eventDto = StockEventDtoBuilder.createStockEventDto();

    eventDto.setFacilityId(UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060"));
    eventDto.setProgramId(UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3"));
    //the following source id is not in valid destination list
    eventDto.setDestinationId(UUID.fromString("0bd28568-43f1-4836-934d-ec5fb11398e8"));
    eventDto.setSourceId(null);

    mockFacilityRefDataService(eventDto);

    //when
    try {
      sourceDestinationValidator.validate(eventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.asMessage().toString(), containsString(ERROR_DESTINATION_NOT_VALID));
      return;
    }

    Assert.fail();
  }

  private void mockFacilityRefDataService(StockEventDto eventDto) {
    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    facilityTypeDto.setId(UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060"));
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setType(facilityTypeDto);
    when(facilityReferenceDataService.findOne(eventDto.getFacilityId())).thenReturn(facilityDto);
  }
}