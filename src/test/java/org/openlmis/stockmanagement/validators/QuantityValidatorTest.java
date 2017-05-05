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

import static java.util.UUID.randomUUID;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class QuantityValidatorTest {

  @Rule
  public ExpectedException expectedEx = none();

  @InjectMocks
  private QuantityValidator quantityValidator;

  @Mock
  private StockCardRepository stockCardRepository;

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @Test
  public void should_not_throw_validation_exception_if_event_has_no_destination_or_debit_reason()
      throws Exception {
    //given
    StockEventDto stockEventDto = new StockEventDto();

    //when
    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void should_not_throw_validation_exception_if_event_reason_id_is_not_found()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    when(reasonRepository.findOne(stockEventDto.getLineItems().get(0).getReasonId()))
        .thenReturn(null);

    //when
    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void should_not_throw_exception_if_event_line_item_has_no_reason()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(randomUUID());
    stockEventDto.getLineItems().get(0).setReasonId(null);

    //when
    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void should_throw_validation_exception_if_quantity_make_soh_below_zero()
      throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH);

    //given
    ZonedDateTime day1 = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime day2 = day1.plusDays(1);
    ZonedDateTime day3 = day2.plusDays(1);
    ZonedDateTime day4 = day3.plusDays(1);

    ArrayList<StockCardLineItem> lineItems = new ArrayList<>();
    lineItems.add(createCreditLineItem(day1, 5));
    lineItems.add(createDebitLineItem(day3, 1));
    lineItems.add(createCreditLineItem(day4, 2));

    StockCard card = new StockCard();
    card.setLineItems(lineItems);

    StockEventDto stockEventDto = createDebitEventDto(day2, 5);

    when(stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableIdAndLotId(
            stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(),
            stockEventDto.getLineItems().get(0).getOrderableId(),
            stockEventDto.getLineItems().get(0).getLotId()))
        .thenReturn(card);

    //when
    quantityValidator.validate(stockEventDto);
  }

  private StockEventDto createDebitEventDto(ZonedDateTime day2, int quantity) {
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(randomUUID());
    stockEventDto.getLineItems().get(0).setQuantity(quantity);
    stockEventDto.getLineItems().get(0).setOccurredDate(day2);
    return stockEventDto;
  }

  private StockCardLineItem createDebitLineItem(ZonedDateTime dateTime, int quantity) {
    StockCardLineItem lineItem2 = new StockCardLineItem();
    lineItem2.setQuantity(quantity);
    lineItem2.setOccurredDate(dateTime);
    lineItem2.setDestination(new Node());
    return lineItem2;
  }

  private StockCardLineItem createCreditLineItem(ZonedDateTime dateTime, int quantity) {
    StockCardLineItem lineItem1 = new StockCardLineItem();
    lineItem1.setQuantity(quantity);
    lineItem1.setOccurredDate(dateTime);
    lineItem1.setSource(new Node());
    return lineItem1;
  }
}