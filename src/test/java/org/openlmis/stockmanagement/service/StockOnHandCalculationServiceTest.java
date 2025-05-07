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

package org.openlmis.stockmanagement.service;

import static java.lang.Integer.MAX_VALUE;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERRRO_EVENT_SOH_EXCEEDS_LIMIT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class StockOnHandCalculationServiceTest {

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private OrderableReferenceDataService orderableService;

  @InjectMocks
  private StockOnHandCalculationService stockOnHandCalculationService;

  private StockCardLineItemReason creditAdjustmentReason;

  private StockCardLineItemReason debitAdjustmentReason;

  private LocalDate firstDate = LocalDate.of(2015, 1, 1);

  @Before
  public void setUp() throws Exception {

    creditAdjustmentReason = new StockCardLineItemReasonDataBuilder()
        .withAdjustmentCategory()
        .build();

    debitAdjustmentReason = new StockCardLineItemReasonDataBuilder()
        .withAdjustmentCategory()
        .withDebitType()
        .build();
  }

  @Test
  public void shouldThrowExceptionWhenQuantityMakesStockOnHandBelowZero() {
    final UUID orderableId = UUID.randomUUID();
    mockOrderable(orderableId);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH);

    StockCard card = new StockCard();
    card.setOrderableId(orderableId);
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate.plusDays(1), 5, card),
        createDebitLineItem(firstDate.plusDays(3), 1, card),
        createDebitLineItem(firstDate.plusDays(2), 5, card),
        createCreditLineItem(firstDate.plusDays(4), 2, card)
    ));

    stockOnHandCalculationService.calculateStockOnHand(card);
  }

  @Test
  public void shouldReturnWhenStockOnHandIsNotBelowZero() {
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate.plusDays(1), 10, card),
        createDebitLineItem(firstDate.plusDays(2), 5, card)
    ));
    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(Integer.valueOf(5), card.getStockOnHand());
  }

  @Test
  public void shouldCalculateSohOfLineItemWithCreditReason() {
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 10, card)
    ));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(Integer.valueOf(10), card.getStockOnHand());
  }

  @Test
  public void shouldNotIncreaseSoHOverIntLimit() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERRRO_EVENT_SOH_EXCEEDS_LIMIT);

    int quantityToAdd = 10;
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, MAX_VALUE - quantityToAdd + 1, card),
        createCreditLineItem(firstDate.plusDays(1), quantityToAdd, card)
    ));

    stockOnHandCalculationService.calculateStockOnHand(card);
  }

  @Test
  public void shouldDecreaseSoHOfLineItemWithDebitReason() {
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 15, card),
        createDebitLineItem(firstDate.plusDays(1), 5, card)
    ));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(Integer.valueOf(10), card.getStockOnHand());
  }

  @Test
  public void shouldIncreaseSoHOfLineItemWhenReceiveFrom() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .source(new Node())
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).build();

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 15, card),
        lineItem));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(Integer.valueOf(30), card.getStockOnHand());
  }

  @Test
  public void shouldDecreaseSoHOfLineItemWhenIssueTo() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .destination(new Node())
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).build();

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 15, card),
        lineItem));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(Integer.valueOf(0), card.getStockOnHand());
  }

  @Test
  public void shouldAssignCreditReasonAndReturnQuantityAsSohForPhysicalOverstock() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).build();
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 10, card),
        lineItem));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(lineItem.getStockOnHand(), Integer.valueOf(15));
    assertEquals(card.getStockOnHand(), Integer.valueOf(15));
    assertEquals(ReasonType.CREDIT, lineItem.getReason().getReasonType());
    assertEquals(ReasonCategory.PHYSICAL_INVENTORY, lineItem.getReason().getReasonCategory());
  }

  @Test
  public void shouldAssignDebitReasonAndReturnQuantityAsSohForPhysicalUnderStock() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).build();
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 20, card),
        lineItem));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(lineItem.getStockOnHand(), Integer.valueOf(15));
    assertEquals(card.getStockOnHand(), Integer.valueOf(15));
    assertEquals(ReasonType.DEBIT, lineItem.getReason().getReasonType());
    assertEquals(ReasonCategory.PHYSICAL_INVENTORY, lineItem.getReason().getReasonCategory());
  }

  @Test
  public void shouldAssignBalanceReasonAndReturnQuantityAsSohForPhysicalBalance() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).build();
    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, 15, card),
        lineItem));

    card = stockOnHandCalculationService.calculateStockOnHand(card);

    assertEquals(lineItem.getStockOnHand(), Integer.valueOf(15));
    assertEquals(card.getStockOnHand(), Integer.valueOf(15));
    assertEquals(ReasonType.BALANCE_ADJUSTMENT, lineItem.getReason().getReasonType());
    assertEquals(ReasonCategory.PHYSICAL_INVENTORY, lineItem.getReason().getReasonCategory());
  }

  private StockCardLineItem createDebitLineItem(LocalDate date, int quantity, StockCard card) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .reason(debitAdjustmentReason)
        .stockCard(card)
        .build();
  }

  private StockCardLineItem createCreditLineItem(LocalDate date, int quantity, StockCard card) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .reason(creditAdjustmentReason)
        .stockCard(card)
        .build();
  }

  private void mockOrderable(UUID orderableId) {
    OrderableDto dto = new OrderableDto();
    dto.setId(orderableId);
    dto.setProductCode("TEST");
    when(orderableService.findOne(orderableId)).thenReturn(dto);
  }
}
