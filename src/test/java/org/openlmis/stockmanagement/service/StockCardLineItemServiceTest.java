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

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERRRO_EVENT_SOH_EXCEEDS_LIMIT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;

/**
 * The type Stock card line item service test.
 */
@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class StockCardLineItemServiceTest {

  /**
   * The Expected exception.
   */
  @Rule
  public ExpectedException expectedException = none();

  @InjectMocks
  private StockCardLineItemService stockCardLineItemService;

  private StockCardLineItemReason creditAdjustmentReason;

  private StockCardLineItemReason debitAdjustmentReason;

  private LocalDate firstDate = LocalDate.of(2015, 1, 1);

  /**
   * Sets up.
   *
   * @throws Exception the exception
   */
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

  /**
   * Should skip with null line items.
   */
  @Test
  public void shouldSkipWithNullLineItems() {
    StockCard card = new StockCard();

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertNull(card.getLineItems());
  }

  /**
   * Should skip with empty line items.
   */
  @Test
  public void shouldSkipWithEmptyLineItems() {
    StockCard card = new StockCard();
    card.setLineItems(newArrayList());

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertNotNull(card.getLineItems());
    assertTrue(card.getLineItems().isEmpty());
  }

  /**
   * Should calculate stock card line item so h with credit reason.
   */
  @Test
  public void shouldCalculateStockCardLineItemSoHWithCreditReason() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem = createPhysicalInventoryLineItem(firstDate, 10, card);
    card.setLineItems(newArrayList(lineItem));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(10), lineItem.getStockOnHand());
  }

  /**
   * Should not increase stock card line item so h over int limit.
   */
  @Test
  public void shouldNotIncreaseStockCardLineItemSoHOverIntLimit() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERRRO_EVENT_SOH_EXCEEDS_LIMIT);

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createCreditLineItem(firstDate, Integer.MAX_VALUE, card),
        createCreditLineItem(firstDate.plusDays(1), 1, card)
    ));

    stockCardLineItemService.populateStockOnHandLineItems(card);
  }

  /**
   * Should not increase stock card line item so h under int limit.
   */
  @Test
  public void shouldNotIncreaseStockCardLineItemSoHUnderIntLimit() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERRRO_EVENT_SOH_EXCEEDS_LIMIT);

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(
        createDebitLineItem(firstDate, Integer.MIN_VALUE, card),
        createDebitLineItem(firstDate.plusDays(1), 1, card)
    ));

    stockCardLineItemService.populateStockOnHandLineItems(card);
  }

  /**
   * Should decrease stock card line item so h with debit reason.
   */
  @Test
  public void shouldDecreaseStockCardLineItemSoHWithDebitReason() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem1 = createCreditLineItem(firstDate, 15, card);
    StockCardLineItem lineItem2 = createDebitLineItem(firstDate.plusDays(1), 5, card);
    card.setLineItems(newArrayList(lineItem1, lineItem2));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(15), lineItem1.getStockOnHand());
    assertEquals(Integer.valueOf(10), lineItem2.getStockOnHand());
  }

  /**
   * Should increase stock card line item so h when receive from.
   */
  @Test
  public void shouldIncreaseStockCardLineItemSoHWhenReceiveFrom() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem1 = createCreditLineItem(firstDate, 15, card);
    StockCardLineItem lineItem2 = StockCardLineItem.builder()
        .source(new Node())
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).stockCard(card).build();
    card.setLineItems(newArrayList(lineItem1, lineItem2));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(15), lineItem1.getStockOnHand());
    assertEquals(Integer.valueOf(30), lineItem2.getStockOnHand());
  }

  /**
   * Should decrease stock card line item so h when issue to.
   */
  @Test
  public void shouldDecreaseStockCardLineItemSoHWhenIssueTo() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem1 = createCreditLineItem(firstDate, 15, card);
    StockCardLineItem lineItem2 = StockCardLineItem.builder()
        .destination(new Node())
        .occurredDate(firstDate.plusDays(1))
        .quantity(15).stockCard(card).build();
    card.setLineItems(newArrayList(lineItem1, lineItem2));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(15), lineItem1.getStockOnHand());
    assertEquals(Integer.valueOf(0), lineItem2.getStockOnHand());
  }

  /**
   * Should assign credit reason and return quantity as soh for physical overstock.
   */
  @Test
  public void shouldAssignCreditReasonAndReturnQuantityAsSohForPhysicalOverstock() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem1 = createCreditLineItem(firstDate, 10, card);
    StockCardLineItem lineItem2 = createPhysicalInventoryLineItem(firstDate.plusDays(1), 15, card);
    card.setLineItems(newArrayList(lineItem1, lineItem2));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(15), lineItem2.getStockOnHand());
    assertEquals(ReasonType.CREDIT, lineItem2.getReason().getReasonType());
    assertEquals(ReasonCategory.PHYSICAL_INVENTORY, lineItem2.getReason().getReasonCategory());
  }

  /**
   * Should assign debit reason and return quantity as soh for physical under stock.
   */
  @Test
  public void shouldAssignDebitReasonAndReturnQuantityAsSohForPhysicalUnderStock() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem1 = createCreditLineItem(firstDate, 20, card);
    StockCardLineItem lineItem2 = createPhysicalInventoryLineItem(firstDate.plusDays(1), 15, card);
    card.setLineItems(newArrayList(lineItem1, lineItem2));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(15), lineItem2.getStockOnHand());
    assertEquals(ReasonType.DEBIT, lineItem2.getReason().getReasonType());
    assertEquals(ReasonCategory.PHYSICAL_INVENTORY, lineItem2.getReason().getReasonCategory());
  }

  /**
   * Should assign balance reason and return quantity as soh for physical balance.
   */
  @Test
  public void shouldAssignBalanceReasonAndReturnQuantityAsSohForPhysicalBalance() {
    StockCard card = new StockCard();
    StockCardLineItem lineItem1 = createCreditLineItem(firstDate, 15, card);
    StockCardLineItem lineItem2 = createPhysicalInventoryLineItem(firstDate.plusDays(1), 15, card);
    card.setLineItems(newArrayList(lineItem1, lineItem2));

    stockCardLineItemService.populateStockOnHandLineItems(card);

    assertEquals(Integer.valueOf(15), lineItem2.getStockOnHand());
    assertEquals(ReasonType.BALANCE_ADJUSTMENT, lineItem2.getReason().getReasonType());
    assertEquals(ReasonCategory.PHYSICAL_INVENTORY, lineItem2.getReason().getReasonCategory());
  }

  private StockCardLineItem createPhysicalInventoryLineItem(LocalDate date, int quantity,
      StockCard card) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .stockCard(card)
        .build();
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
}
