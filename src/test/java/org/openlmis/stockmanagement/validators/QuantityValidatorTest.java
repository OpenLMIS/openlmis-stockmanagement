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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER;
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
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
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
  public void shouldNotThrowValidationExceptionIfEventHasNoDestinationOrDebitReason()
      throws Exception {
    //given
    StockEventDto stockEventDto = new StockEventDto();

    //when
    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotThrowValidationExceptionIfEventReasonIdIsNotFound()
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
  public void shouldNotThrowExceptionIfEventLineItemHasNoReason()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(randomUUID());
    stockEventDto.getLineItems().get(0).setReasonId(null);

    //when
    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldThrowValidationExceptionIfQuantityMakeSohBelowZero()
      throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH);

    //given
    ZonedDateTime day1 = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
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

  @Test
  public void shouldNotRejectWhenStockOnHandWithAdjustmentsMatchQuantity()
      throws Exception {
    //given
    ZonedDateTime day1 = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime day2 = day1.plusDays(1);
    ZonedDateTime day3 = day2.plusDays(1);

    List<StockCardLineItem> cardItems = new ArrayList<>();
    cardItems.add(generateLineItemWithAdjustments(day1, 10));
    cardItems.add(generateLineItemWithAdjustments(day3, 10, -2, -3));

    StockCard card = new StockCard();
    card.setLineItems(cardItems);

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

  @Test
  public void shouldRejectWhenStockOnHandWithAdjustmentsDoesNotMatchQuantity()
      throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER);

    //given
    ZonedDateTime day1 = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime day2 = day1.plusDays(1);
    ZonedDateTime day3 = day2.plusDays(1);

    List<StockCardLineItem> cardItems = new ArrayList<>();
    cardItems.add(generateLineItemWithAdjustments(day1, 10));
    cardItems.add(generateLineItemWithAdjustments(day3, 5, 1, 3));

    StockCard card = new StockCard();
    card.setLineItems(cardItems);

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

  @Test
  public void shouldRejectWhenAnyAdjustmentHasNegativeQuantity()
      throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);

    //given
    ZonedDateTime day1 = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime day2 = day1.plusDays(1);

    StockAdjustment invalidAdjustment = new StockAdjustment(
        StockCardLineItemReason.physicalDebit(), -5);
    StockCardLineItem invalidLineItem = createCreditLineItem(day1, 10);
    invalidLineItem.getStockAdjustments().add(invalidAdjustment);

    StockCard card = new StockCard();
    card.setLineItems(Collections.singletonList(invalidLineItem));

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

  private StockCardLineItem generateLineItemWithAdjustments(
      ZonedDateTime date, int quantity, int... adjustments) {
    int absQuantity = Math.abs(quantity);
    StockCardLineItem item = quantity < 0
        ? createDebitLineItem(date, absQuantity) : createCreditLineItem(date, absQuantity);

    for (int value : adjustments) {
      StockCardLineItemReason reason = value < 0
          ? StockCardLineItemReason.physicalDebit() : StockCardLineItemReason.physicalCredit();
      item.getStockAdjustments().add(new StockAdjustment(reason, Math.abs(value)));
    }

    return item;
  }

  private StockEventDto createDebitEventDto(ZonedDateTime dateTime, int quantity) {
    StockEventDto stockEventDto = createStockEventDto();

    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(randomUUID());
    stockEventDto.getLineItems().get(0).setQuantity(quantity);
    stockEventDto.getLineItems().get(0).setOccurredDate(dateTime);

    return stockEventDto;
  }

  private StockCardLineItem createDebitLineItem(ZonedDateTime dateTime, int quantity) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(dateTime)
        .processedDate(dateTime)
        .destination(new Node())
        .reason(StockCardLineItemReason.physicalDebit())
        .stockAdjustments(new ArrayList<>())
        .build();
  }

  private StockCardLineItem createCreditLineItem(ZonedDateTime dateTime, int quantity) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(dateTime)
        .processedDate(dateTime)
        .destination(new Node())
        .reason(StockCardLineItemReason.physicalCredit())
        .stockAdjustments(new ArrayList<>())
        .build();
  }

}