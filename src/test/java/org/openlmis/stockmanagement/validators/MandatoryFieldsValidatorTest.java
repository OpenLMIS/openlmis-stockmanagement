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
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_FACILITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_NO_LINE_ITEMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_OCCURRED_DATE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_OCCURRED_DATE_IN_FUTURE;
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
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class MandatoryFieldsValidatorTest extends BaseValidatorTest  {

  @InjectMocks
  private MandatoryFieldsValidator mandatoryFieldsValidator;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private StockEventDto stockEventDto;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    stockEventDto = StockEventDtoDataBuilder.createStockEventDto();

    when(facilityService.findOne(stockEventDto.getFacilityId())).thenReturn(new FacilityDto());
    when(programService.findOne(stockEventDto.getProgramId())).thenReturn(new ProgramDto());

    setContext(stockEventDto);
  }

  @Test
  public void stock_event_with_incorrect_facility_id_should_not_pass_validation() throws Exception {
    UUID facilityId = UUID.randomUUID();
    when(facilityService.findOne(facilityId)).thenReturn(null);

    expectFacilityException(facilityId);
  }

  @Test
  public void stock_event_with_incorrect_program_id_should_not_pass_validation() throws Exception {
    UUID programId = UUID.randomUUID();
    when(programService.findOne(programId)).thenReturn(null);

    expectProgramException(programId);
  }

  @Test()
  public void stock_event_without_orderable_should_not_pass_validation() throws Exception {
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
    expectOccurredDateException(null,
        ERROR_EVENT_OCCURRED_DATE_INVALID);
  }

  @Test()
  public void stock_event_with_future_occurred_date_should_not_pass_validation() throws Exception {
    //given
    expectOccurredDateException(LocalDate.now().plusDays(1),
        ERROR_EVENT_OCCURRED_DATE_IN_FUTURE);
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

  @Test
  public void should_not_pass_if_no_line_items() throws Exception {
    expectLineItemsError(null);
  }

  @Test
  public void should_not_pass_if_empty_line_items() throws Exception {
    expectLineItemsError(emptyList());
  }

  private void expectLineItemsError(List<StockEventLineItem> lineItems) {
    stockEventDto.setLineItems(lineItems);
    expectedEx.expectMessage(ERROR_EVENT_NO_LINE_ITEMS);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectFacilityException(UUID facilityId) {
    //given
    stockEventDto.setFacilityId(facilityId);
    setContext(stockEventDto);

    //when
    String suffix = facilityId != null ? facilityId.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_FACILITY_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectProgramException(UUID programId) {
    //given
    stockEventDto.setProgramId(programId);
    setContext(stockEventDto);

    //when
    String suffix = programId != null ? programId.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_PROGRAM_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectOrderableException(UUID orderableId) {
    //given
    stockEventDto.getLineItems().get(0).setOrderableId(orderableId);

    //when
    String suffix = orderableId != null ? orderableId.toString() : "";
    expectedEx.expectMessage(ERROR_EVENT_ORDERABLE_INVALID + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectOccurredDateException(LocalDate occurredDate, String errorKey) {
    //given
    stockEventDto.getLineItems().get(0).setOccurredDate(occurredDate);

    //when
    String suffix = occurredDate != null ? occurredDate.toString() : "";
    expectedEx.expectMessage(errorKey + ": " + suffix);
    mandatoryFieldsValidator.validate(stockEventDto);
  }

  private void expectQuantityException(Integer quantity) {
    //given
    List<StockEventLineItem> lineItems = stockEventDto.getLineItems();
    lineItems.get(0).setQuantity(quantity);

    //when
    expectedEx.expectMessage(ERROR_EVENT_QUANTITY_INVALID + ": " + lineItems);
    mandatoryFieldsValidator.validate(stockEventDto);
  }
}