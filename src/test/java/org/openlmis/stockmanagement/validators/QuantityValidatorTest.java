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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED;
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
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class QuantityValidatorTest {

  @Rule
  public ExpectedException expectedException = none();

  @InjectMocks
  private QuantityValidator quantityValidator;

  @Mock
  private StockCardRepository stockCardRepository;

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @Mock
  private StockoutNotifier stockoutNotifier;

  @Test
  public void shouldRejectWhenQuantityMakesStockOnHandBelowZero() throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH);

    //given
    ZonedDateTime firstDate = dateTimeFromYear(2015);

    StockCard card = new StockCard();
    card.setLineItems(Arrays.asList(
        createCreditLineItem(firstDate.plusDays(1), 5),
        createDebitLineItem(firstDate.plusDays(3), 1),
        createCreditLineItem(firstDate.plusDays(4), 2)
    ));

    StockEventDto event = createDebitEventDto(firstDate.plusDays(2), 5);
    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenEventHasNoDestinationOrDebitReason() throws Exception {
    //given
    StockEventDto stockEventDto = new StockEventDto();

    //when
    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotRejectWhenEventReasonIdIsNotFound() throws Exception {
    //given
    StockEventDto event = createStockEventDto();

    StockEventLineItem invalidItem = event.getLineItems().get(0);
    invalidItem.setDestinationId(null);

    when(reasonRepository.findOne(invalidItem.getReasonId())).thenReturn(null);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenEventLineItemHasNoReason() throws Exception {
    //given
    StockEventDto event = createStockEventDto();

    StockEventLineItem invalidItem = event.getLineItems().get(0);
    invalidItem.setDestinationId(randomUUID());
    invalidItem.setReasonId(null);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandMatchesQuantityAndNoAdjustments() throws Exception {
    //given
    ZonedDateTime firstDate = dateTimeFromYear(2015);

    StockCard card = new StockCard();
    StockEventDto event = createDebitEventDto(firstDate.plusDays(1), 0);

    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandWithAdjustmentsMatchesQuantity() throws Exception {
    //given
    ZonedDateTime firstDate = dateTimeFromYear(2015);

    StockCard card = new StockCard();
    card.setLineItems(Arrays.asList(
        generateLineItemWithAdjustments(firstDate.plusDays(1), 10),
        generateLineItemWithAdjustments(firstDate.plusDays(3), 10, -2, -3)
    ));

    StockEventDto event = createDebitEventDto(firstDate.plusDays(2), 5);
    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldRejectWhenAnyAdjustmentHasNegativeQuantity() throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);

    //given
    ZonedDateTime firstDate = dateTimeFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 15, Collections.singletonList(createCreditAdjustment(-2)));

    StockCard card = new StockCard();
    card.setLineItems(Collections.singletonList(lineItem));

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(2), 5));
    given(event.isPhysicalInventory()).willReturn(true);
    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldRejectWhenStockOnHandDoesNotMatchQuantityAndNoAdjustmentsProvided()
      throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED);

    //given
    ZonedDateTime firstDate = dateTimeFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(firstDate.plusDays(1), 15);
    lineItem.setStockOnHand(50);

    StockCard card = new StockCard();
    card.setLineItems(Collections.singletonList(lineItem));

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(2), 5));
    given(event.isPhysicalInventory()).willReturn(true);
    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldRejectWhenStockOnHandWithAdjustmentsDoesNotMatchQuantity() throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER);

    //given
    ZonedDateTime firstDate = dateTimeFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 15, Collections.singletonList(createCreditAdjustment(5)));
    lineItem.setStockOnHand(5);

    StockCard card = new StockCard();
    card.setLineItems(Collections.singletonList(lineItem));

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(2), 5));
    given(event.isPhysicalInventory()).willReturn(true);
    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldCallStockoutNotifierWhenStockOnHandIsZero() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setQuantity(0);
    StockCard card = new StockCard();
    mockCardFound(stockEventDto, card);

    //when
    quantityValidator.validate(stockEventDto);

    //then
    verify(stockoutNotifier).notifyStockEditors(any(StockCard.class));
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

  private ZonedDateTime dateTimeFromYear(int year) {
    return ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
  }
  
  private StockEventDto createDebitEventDto(ZonedDateTime dateTime, int quantity) {
    StockEventDto stockEventDto = createStockEventDto();

    stockEventDto.getLineItems().get(0).setDestinationId(randomUUID());
    stockEventDto.getLineItems().get(0).setQuantity(quantity);
    stockEventDto.getLineItems().get(0).setOccurredDate(dateTime);
    stockEventDto.getLineItems().get(0).setSourceId(null);

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

  private StockCardLineItem createCreditLineItem(
      ZonedDateTime dateTime, int quantity, List<StockAdjustment> adjustments) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(dateTime)
        .processedDate(dateTime)
        .destination(new Node())
        .reason(StockCardLineItemReason.physicalCredit())
        .stockAdjustments(adjustments)
        .build();
  }

  private StockCardLineItem createCreditLineItem(ZonedDateTime dateTime, int quantity) {
    return createCreditLineItem(dateTime, quantity, new ArrayList<>());
  }

  private StockAdjustment createCreditAdjustment(int quantity) {
    return StockAdjustment
        .builder()
        .reason(StockCardLineItemReason.physicalCredit())
        .quantity(quantity)
        .build();
  }

  private void mockCardFound(StockEventDto event, StockCard card) {
    when(stockCardRepository
        .findByProgramIdAndFacilityIdAndOrderableIdAndLotId(
            event.getProgramId(),
            event.getFacilityId(),
            event.getLineItems().get(0).getOrderableId(),
            event.getLineItems().get(0).getLotId()))
        .thenReturn(card);
  }
}