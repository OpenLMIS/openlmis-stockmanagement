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

import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.adjustment.ReasonCategory;
import org.openlmis.stockmanagement.domain.adjustment.ReasonType;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;

@RunWith(MockitoJUnitRunner.class)
public class AdjustmentValidatorTest {

  @InjectMocks
  private AdjustmentValidator adjustmentValidator;

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void incorrect_reason_type_should_not_pass_when_event_has_no_source_and_destination()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createNoSourceDestinationStockEventDto();

    StockCardLineItemReason reason = StockCardLineItemReason
        .builder()
        .reasonType(ReasonType.BALANCE_ADJUSTMENT)
        .build();
    when(reasonRepository.findOne(stockEventDto.getReasonId())).thenReturn(reason);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID + ": " + reason.getReasonType());

    //when
    adjustmentValidator.validate(stockEventDto);
  }

  @Test
  public void incorrect_reason_category_should_not_pass_when_event_has_no_source_and_destination()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createNoSourceDestinationStockEventDto();

    StockCardLineItemReason reason = StockCardLineItemReason
        .builder()
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.AD_HOC)
        .build();
    when(reasonRepository.findOne(stockEventDto.getReasonId())).thenReturn(reason);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID + ": " + reason.getReasonCategory());

    //when
    adjustmentValidator.validate(stockEventDto);
  }

  @Test
  public void should_not_throw_validation_exception_if_event_has_no_source_destination_and_reason()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createNoSourceDestinationStockEventDto();
    stockEventDto.setReasonId(null);

    //when
    adjustmentValidator.validate(stockEventDto);
  }
}