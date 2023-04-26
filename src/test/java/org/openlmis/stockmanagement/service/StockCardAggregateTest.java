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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

@SuppressWarnings("PMD.TooManyMethods")
public class StockCardAggregateTest {

  private StockCard stockCard1;
  private StockCard stockCard2;
  private StockCardAggregate stockCardAggregate;
  private String tag1 = "tag1";
  private String tag2 = "tag2";
  private String tag3 = "tag3";
  private String tag4 = "tag4";

  @Before
  public void setUp() {
    StockEvent event = new StockEventDataBuilder().build();
    stockCard1 = new StockCardDataBuilder(event)
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.CREDIT,
                            singletonList(tag1),
                            10,
                            LocalDate.of(2018, 5, 10)))
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.DEBIT,
                            singletonList(tag2),
                            10,
                            LocalDate.of(2018, 5, 11)))
            .build();
    stockCard2 = new StockCardDataBuilder(event)
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.CREDIT,
                            singletonList(tag1),
                            20,
                            LocalDate.of(2018, 5, 12)))
            .build();
    StockCard stockCard3 = new StockCardDataBuilder(event)
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.CREDIT,
                            asList(tag3, tag1),
                            30,
                            LocalDate.of(2018, 5, 13)))
            .build();
    StockCard stockCard4 = new StockCardDataBuilder(event)
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.CREDIT,
                            singletonList(tag4),
                            30,
                            LocalDate.of(2018, 6, 15)))
            .build();
    StockCard stockCard5 = new StockCardDataBuilder(event)
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.CREDIT,
                            singletonList(tag4),
                            30,
                            LocalDate.of(2018, 7, 1)))
            .build();
    StockCard stockCard6 = new StockCardDataBuilder(event)
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.CREDIT,
                            singletonList(tag4),
                            10,
                            LocalDate.of(2018, 8, 1)))
            .withLineItem(new StockCardLineItemDataBuilder()
                    .buildWithReasonTypeAndTagsAndQuantityAndOccuredDate(
                            ReasonType.DEBIT,
                            singletonList(tag4),
                            10,
                            LocalDate.of(2018, 8, 31)))
            .build();


    List<CalculatedStockOnHand> calculatedStockOnHands = new ArrayList<>(asList(
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard1)
                    .withOccurredDate(LocalDate.of(2018, 5, 10))
                    .withStockOnHand(10)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard1)
                    .withOccurredDate(LocalDate.of(2018, 5, 11))
                    .withStockOnHand(0)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard2)
                    .withOccurredDate(LocalDate.of(2018, 5, 12))
                    .withStockOnHand(20)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard3)
                    .withOccurredDate(LocalDate.of(2018, 5, 13))
                    .withStockOnHand(50)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard4)
                    .withOccurredDate(LocalDate.of(2018, 6, 15))
                    .withStockOnHand(0)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard5)
                    .withOccurredDate(LocalDate.of(2018, 7, 1))
                    .withStockOnHand(0)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard6)
                    .withOccurredDate(LocalDate.of(2018, 8, 1))
                    .withStockOnHand(0)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard6)
                    .withOccurredDate(LocalDate.of(2018, 8, 31))
                    .withStockOnHand(0)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard6)
                    .withOccurredDate(LocalDate.of(2018, 10, 31))
                    .withStockOnHand(100)
                    .build(),
            new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard6)
                    .withOccurredDate(LocalDate.of(2018, 12, 15))
                    .withStockOnHand(0)
                    .build()
    ));

    stockCardAggregate = new StockCardAggregate(
            asList(stockCard1, stockCard2, stockCard3,
                    stockCard4, stockCard5, stockCard6), calculatedStockOnHands);
  }

  @Test
  public void shouldGetAmountForTag() {
    assertEquals(new Integer(60), stockCardAggregate.getAmount(tag1, null, null));
    assertEquals(new Integer(-10), stockCardAggregate.getAmount(tag2, null, null));
    assertEquals(new Integer(30), stockCardAggregate.getAmount(tag3, null, null));
  }

  @Test
  public void shouldGetAmountForTagInRange() {
    assertEquals(new Integer(60), stockCardAggregate.getAmount(tag1,
            LocalDate.of(2018, 5, 10), LocalDate.of(2018, 5, 13)));
    assertEquals(new Integer(30), stockCardAggregate.getAmount(tag1,
            LocalDate.of(2018, 5, 10), LocalDate.of(2018, 5, 12)));
    assertEquals(new Integer(30), stockCardAggregate.getAmount(tag1,
            LocalDate.of(2018, 5, 13), LocalDate.of(2018, 5, 13)));
  }

  @Test
  public void shouldReturnNullIfThereIsNoLineItemsAvailableForTag() {
    assertEquals(new Integer(0), stockCardAggregate.getAmount("some-tag", null, null));
  }

  @Test
  public void shouldReturnZeroIfThereIsNoLineItemsAvailableInRange() {
    assertEquals(new Integer(0), stockCardAggregate.getAmount("tag1",
            LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2)));
  }

  @Test
  public void shouldGetAmounts() {
    Map<String, Integer> amounts = stockCardAggregate.getAmounts(null, null);

    assertEquals(new Integer(60), amounts.get(tag1));
    assertEquals(new Integer(-10), amounts.get(tag2));
    assertEquals(new Integer(30), amounts.get(tag3));
  }

  @Test
  public void shouldGetAmountsInRange() {
    Map<String, Integer> amounts = stockCardAggregate.getAmounts(
            LocalDate.of(2018, 5, 10), LocalDate.of(2018, 5, 11));

    assertEquals(new Integer(10), amounts.get(tag1));
    assertEquals(new Integer(-10), amounts.get(tag2));
    assertNull(amounts.get(tag3));
  }

  @Test
  public void shouldGetStockOutDaysInRange() {
    assertEquals(new Long(1), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 5, 10), LocalDate.of(2018, 5, 11)));
    assertEquals(new Long(1), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 5, 11), LocalDate.of(2018, 5, 11)));
    assertEquals(new Long(1), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 5, 11), LocalDate.of(2018, 5, 12)));
    assertEquals(new Long(0), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 5, 12), LocalDate.of(2018, 5, 13)));

    assertEquals(new Long(16), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 6, 1), LocalDate.of(2018, 6, 30)));

    assertEquals(new Long(30), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 7, 1), LocalDate.of(2018, 7, 31)));

    assertEquals(new Long(30), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 31)));

    assertEquals(new Long(29), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 9, 1), LocalDate.of(2018, 9, 30)));

    assertEquals(new Long(0), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30)));

    assertEquals(new Long(16), stockCardAggregate.getStockoutDays(
            LocalDate.of(2018, 12, 1), LocalDate.of(2018, 12, 31)));

    assertEquals(new Long(30), stockCardAggregate.getStockoutDays(
            LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31)));
  }

  @Test
  public void shouldIncludeStockoutToCurrentDay() {
    LocalDate date = LocalDate.of(2019, 11, 15);

    stockCardAggregate.getCalculatedStockOnHands().add(new CalculatedStockOnHandDataBuilder()
            .withStockCard(stockCard1)
            .withOccurredDate(date)
            .withStockOnHand(0)
            .build());

    long stockoutDaysBeforeDate = stockCardAggregate.getStockoutDays(null, date.minusDays(1));

    assertEquals(
            new Long(stockoutDaysBeforeDate + DAYS.between(date, LocalDate.now())),
            stockCardAggregate.getStockoutDays(null, null));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
            .forClass(StockCardAggregate.class)
            .suppress(Warning.NONFINAL_FIELDS)
            .suppress(Warning.STRICT_INHERITANCE)
            .withPrefabValues(StockCard.class, stockCard1, stockCard2)
            .verify();
  }

  @Test
  public void shouldImplementToString() {
    StockCard stockCard = new StockCardDataBuilder(new StockEvent()).build();
    StockCardAggregate stockCardAggregate = new StockCardAggregate(
            singletonList(stockCard),
            singletonList(new CalculatedStockOnHandDataBuilder()
                    .withStockCard(stockCard)
                    .build()));
    ToStringTestUtils.verify(StockCardAggregate.class, stockCardAggregate, "LOGGER");
  }
}
