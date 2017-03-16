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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_FACILITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_OCCURRED_DATE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_PROGRAM_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_QUANTITY_INVALID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class MandatoryFieldsValidatorTest {

  @InjectMocks
  private MandatoryFieldsValidator mandatoryFieldsValidator;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private StockEventDto stockEventDto;

  @Before
  public void setUp() throws Exception {
    StockEventProcessContext stockEventContext = StockEventProcessContext.builder()
        .facility(new FacilityDto())
        .program(new ProgramDto()).build();
    stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setContext(stockEventContext);
  }

  @Test
  public void stock_event_with_incorrect_facility_id_should_not_pass_validation()
      throws Exception {
    UUID facilityId = UUID.randomUUID();
    stockEventDto.getContext().setFacility(null);

    expectFacilityException(facilityId);
  }

  @Test
  public void stock_event_with_incorrect_program_id_should_not_pass_validation() throws Exception {
    UUID programId = UUID.randomUUID();
    stockEventDto.getContext().setProgram(null);

    expectProgramException(programId);
  }

  @Test()
  public void stock_event_without_orderable_should_not_pass_validation()
      throws Exception {
    expectOrderableException(null);
  }

  @Test
  public void stock_event_with_facility_program_orderable_should_pass_validation()
      throws Exception {
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  @Test()
  public void stock_event_without_occurred_date_should_not_pass_validation()
      throws Exception {
    expectOccurredDateException(null);
  }

  @Test()
  public void stock_event_with_future_occurred_date_should_not_pass_validation() throws Exception {
    //given
    expectOccurredDateException(ZonedDateTime.now().plusDays(1));
  }

  @Test
  public void stock_event_with_right_occurred_date_should_pass_validation() throws Exception {
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  @Test()
  public void stock_event_without_quantity_should_not_pass_validation() throws Exception {
    expectQuantityException(null);
  }

  @Test()
  public void stock_event_with_negative_quantity_should_not_pass_validation() throws Exception {
    expectQuantityException(-100);
  }

  @Test
  public void stock_event_with_positive_or_zero_quantity_should_pass_validation() throws Exception {
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectFacilityException(UUID facilityId) {
    //given
    stockEventDto.setFacilityId(facilityId);

    //when
    String suffix = facilityId != null ? facilityId.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_FACILITY_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectProgramException(UUID programId) {
    //given
    stockEventDto.setProgramId(programId);

    //when
    String suffix = programId != null ? programId.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_PROGRAM_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectOrderableException(UUID orderableId) {
    //given
    stockEventDto.setOrderableId(orderableId);

    //when
    String suffix = orderableId != null ? orderableId.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_ORDERABLE_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectOccurredDateException(ZonedDateTime occurredDate) {
    //given
    stockEventDto.setOccurredDate(occurredDate);

    //when
    String suffix = occurredDate != null ? occurredDate.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_OCCURRED_DATE_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectQuantityException(Integer quantity) {
    //given
    stockEventDto.setQuantity(quantity);

    //when
    String suffix = quantity != null ? quantity.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_QUANTITY_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }
}