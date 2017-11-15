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

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ADJUSTMENTS_NOT_PROVIDED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_STOCK_ON_HAND_CURRENT_STOCK_DIFFER;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class QuantityValidatorTest extends BaseValidatorTest  {

  @Rule
  public ExpectedException expectedException = none();

  @InjectMocks
  private QuantityValidator quantityValidator;

  @Test
  public void shouldRejectWhenQuantityMakesStockOnHandBelowZero() throws Exception {
    //expect
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH);

    //given
    LocalDate firstDate = dateFromYear(2015);

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
    event.setContext(new StockEventProcessContext());

    StockEventLineItem invalidItem = event.getLineItems().get(0);
    invalidItem.setDestinationId(null);

    setContext(event);

    when(reasonRepository.findOne(invalidItem.getReasonId())).thenReturn(null);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenEventLineItemHasNoReason() throws Exception {
    //given
    StockEventDto event = createStockEventDto();
    setContext(event);

    StockEventLineItem invalidItem = event.getLineItems().get(0);
    invalidItem.setDestinationId(randomUUID());
    invalidItem.setReasonId(null);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandMatchesQuantityAndNoAdjustments() throws Exception {
    //given
    LocalDate firstDate = dateFromYear(2015);

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(1), 0));
    when(event.isPhysicalInventory()).thenReturn(true);
    setContext(event);

    //when
    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandWithAdjustmentsMatchesQuantity() throws Exception {
    //given
    LocalDate firstDate = dateFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 10);

    StockCard card = new StockCard();
    card.setLineItems(singletonList(lineItem));

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(2), 5,
        singletonList(createDebitAdjustment(5))));
    given(event.isPhysicalInventory()).willReturn(true);
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
    LocalDate firstDate = dateFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 15);

    StockCard card = new StockCard();
    card.setLineItems(singletonList(lineItem));

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(2), 5,
        singletonList(createCreditAdjustment(-2))));
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
    LocalDate firstDate = dateFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(firstDate.plusDays(1), 15);

    StockCard card = new StockCard();
    card.setLineItems(singletonList(lineItem));

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
    LocalDate firstDate = dateFromYear(2015);

    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 15);

    StockCard card = new StockCard();
    card.setLineItems(singletonList(lineItem));

    StockEventDto event = spy(createDebitEventDto(firstDate.plusDays(2), 5,
        singletonList(createCreditAdjustment(5))));
    given(event.isPhysicalInventory()).willReturn(true);
    mockCardFound(event, card);

    //when
    quantityValidator.validate(event);
  }

  private StockCardLineItem generateLineItemWithAdjustments(
      LocalDate date, int quantity, int... adjustments) {
    int absQuantity = Math.abs(quantity);
    StockCardLineItem item = quantity < 0
        ? createDebitLineItem(date, absQuantity) : createCreditLineItem(date, absQuantity);

    for (int value : adjustments) {
      StockCardLineItemReason reason = value < 0
          ? StockCardLineItemReason.physicalDebit() : StockCardLineItemReason.physicalCredit();
      item.getStockAdjustments().add(
              new PhysicalInventoryLineItemAdjustment(reason, Math.abs(value)));
    }

    return item;
  }

  private LocalDate dateFromYear(int year) {
    return LocalDate.of(year, 1, 1);
  }
  
  private StockEventDto createDebitEventDto(LocalDate date, int quantity,
                                            List<PhysicalInventoryLineItemAdjustment> adjustments) {

    StockEventDto stockEventDto = createStockEventDto();

    stockEventDto.getLineItems().get(0).setDestinationId(randomUUID());
    stockEventDto.getLineItems().get(0).setQuantity(quantity);
    stockEventDto.getLineItems().get(0).setOccurredDate(date);
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setStockAdjustments(adjustments);

    return stockEventDto;
  }

  private StockEventDto createDebitEventDto(LocalDate date, int quantity) {
    return createDebitEventDto(date, quantity, null);
  }

  private StockCardLineItem createDebitLineItem(LocalDate date, int quantity) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .destination(new Node())
        .reason(StockCardLineItemReason.physicalDebit())
        .stockAdjustments(new ArrayList<>())
        .build();
  }

  private StockCardLineItem createCreditLineItem(
      LocalDate date, int quantity, List<PhysicalInventoryLineItemAdjustment> adjustments) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .destination(new Node())
        .reason(StockCardLineItemReason.physicalCredit())
        .stockAdjustments(adjustments)
        .build();
  }

  private StockCardLineItem createCreditLineItem(LocalDate date, int quantity) {
    return createCreditLineItem(date, quantity, new ArrayList<>());
  }

  private PhysicalInventoryLineItemAdjustment createCreditAdjustment(int quantity) {
    return PhysicalInventoryLineItemAdjustment
        .builder()
        .reason(StockCardLineItemReason.physicalCredit())
        .quantity(quantity)
        .build();
  }

  private PhysicalInventoryLineItemAdjustment createDebitAdjustment(int quantity) {
    return PhysicalInventoryLineItemAdjustment
        .builder()
        .reason(StockCardLineItemReason.physicalDebit())
        .quantity(quantity)
        .build();
  }

  private void mockCardFound(StockEventDto event, StockCard card) {
    card.setOrderableId(event.getLineItems().get(0).getOrderableId());
    card.setLotId(event.getLineItems().get(0).getLotId());

    when(stockCardRepository
        .findByProgramIdAndFacilityId(event.getProgramId(), event.getFacilityId()))
        .thenReturn(singletonList(card));

    setContext(event);
  }
}