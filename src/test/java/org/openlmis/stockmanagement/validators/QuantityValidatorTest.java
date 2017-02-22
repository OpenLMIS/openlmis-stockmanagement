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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;

@RunWith(MockitoJUnitRunner.class)
public class QuantityValidatorTest {

  @InjectMocks
  private QuantityValidator quantityValidator;

  @Mock
  private StockCardRepository stockCardRepository;

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @Test
  public void quantity_of_decrease_event_is_greater_than_current_soh_should_not_pass_validation()
      throws Exception {
    StockEventDto stockEventDto = new StockEventDto();
    stockEventDto.setQuantity(200);
    stockEventDto.setDestinationId(UUID.fromString("087e81f6-a74d-4bba-9d01-16e0d64e9609"));

    //1. decrease event is the first event of a stock card
    when(stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableId(
            stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(),
            stockEventDto.getOrderableId()))
        .thenReturn(null);

    //when
    try {
      quantityValidator.validate(stockEventDto);
    } catch (ValidationMessageException ex) {
      //then
      assertThat(ex.getMessage(), containsString(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH));
    }

    //2. decrease event is the subsequent event of a stock card
    StockCard mockedCard = Mockito.mock(StockCard.class);
    when(mockedCard.getStockOnHand()).thenReturn(100);
    when(stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableId(
            stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(),
            stockEventDto.getOrderableId()))
        .thenReturn(mockedCard);
    try {
      quantityValidator.validate(stockEventDto);
    } catch (ValidationMessageException ex) {
      assertThat(ex.getMessage(), containsString(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH));
      return;
    }

    fail();
  }

  @Test
  public void should_not_throw_validation_exception_if_event_has_no_destination_and_debit_reason()
      throws Exception {
    //given
    StockEventDto stockEventDto = new StockEventDto();
    stockEventDto.setQuantity(200);

    StockCard stockCard = StockCard.createStockCardFrom(stockEventDto, UUID.randomUUID());
    when(stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableId(stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(), stockEventDto.getOrderableId()))
        .thenReturn(stockCard);

    //when
    quantityValidator.validate(stockEventDto);

  }

  @Test
  public void should_not_throw_validation_exception_if_event_reason_id_is_not_found()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setDestinationId(null);
    when(reasonRepository.findOne(stockEventDto.getReasonId())).thenReturn(null);

    //when
    quantityValidator.validate(stockEventDto);
  }
}