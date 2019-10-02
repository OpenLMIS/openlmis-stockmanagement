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

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.TooManyMethods")
public class CalculatedStockOnHandServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private CalculatedStockOnHandService calculatedStockOnHandService;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  private UUID facility = randomUUID();
  private UUID program = randomUUID();
  private UUID product = randomUUID();
  private UUID lot = randomUUID();

  private int quantity = 100;
  private int quantity2 = 200;

  private StockCard stockCard;
  private CalculatedStockOnHandDataBuilder calculatedStockOnHandDataBuilder;

  @Before
  public void setUp() {
    StockEvent event = new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .build();
    stockEventsRepository.save(event);

    stockCard = new StockCardDataBuilder(event)
        .withoutId()
        .withOrderable(product)
        .withLot(lot)
        .build();
    stockCardRepository.save(stockCard);

    calculatedStockOnHandDataBuilder = new CalculatedStockOnHandDataBuilder()
        .withoutId()
        .withStockCard(stockCard)
        .withStockOnHand(quantity);
  }

  @Test
  public void shouldGetLatestStockCardsWithStockOnHandWhenDateIsNotSet() {
    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder.build());

    List<StockCard> foundStockCards = calculatedStockOnHandService
        .getStockCardsWithStockOnHand(program, facility);

    assertThat(foundStockCards.size(), is(1));
    assertThat(foundStockCards.get(0).getStockOnHand(), is(quantity));
    assertThat(foundStockCards.get(0).getOccurredDate(), is(LocalDate.now()));
  }

  @Test
  public void shouldGetStockCardsWithStockOnHandWithSpecificDate() {
    LocalDate newDate = LocalDate.of(2018, 5, 10);
    LocalDate oldDate = LocalDate.of(2015, 5, 10);

    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder
        .withOccurredDate(oldDate)
        .build());
    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder
        .withOccurredDate(newDate)
        .withStockOnHand(quantity2)
        .build());

    List<StockCard> foundStockCards = calculatedStockOnHandService
        .getStockCardsWithStockOnHand(program, facility, LocalDate.of(2017,5,10));

    assertThat(foundStockCards.size(), is(1));
    assertThat(foundStockCards.get(0).getStockOnHand(), is(quantity));
    assertThat(foundStockCards.get(0).getOccurredDate(), is(oldDate));
  }

  @Test
  public void shouldGetStockCardsWithMostUpToDateStockOnHand() {
    LocalDate newDate = LocalDate.of(2018, 5, 10);
    LocalDate oldDate = LocalDate.of(2015, 5, 10);

    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder
        .withOccurredDate(newDate)
        .build());
    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder
        .withOccurredDate(oldDate)
        .withStockOnHand(quantity2)
        .build());

    List<StockCard> foundStockCards = calculatedStockOnHandService
        .getStockCardsWithStockOnHand(program, facility, LocalDate.now());

    assertThat(foundStockCards.size(), is(1));
    assertThat(foundStockCards.get(0).getStockOnHand(), is(quantity));
    assertThat(foundStockCards.get(0).getOccurredDate(), is(newDate));
  }
  
  @Test
  public void shouldGetEmptyListIfStockCardsNotFound() {
    List<StockCard> foundStockCards = calculatedStockOnHandService
        .getStockCardsWithStockOnHand(randomUUID(), randomUUID(), LocalDate.now());

    assertThat(foundStockCards.size(), is(0));
  }
  
  @Test
  public void shouldGetStockCardWithNullStockOnHandIfCalculatedStockOnHandNotFound() {
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, LocalDate.now());

    assertThat(stockCard.getStockOnHand(), is(0));
  }

  @Test
  public void shouldSetStockOnHandForStockCard() {
    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder.build());
    
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, LocalDate.now());

    assertThat(stockCard.getStockOnHand(), is(quantity));
  }

  @Test
  public void shouldRecalculateStockOnHandWithCreditReason() {
    final StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withCreditReason()
        .withQuantity(15)
        .build();

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(1))
            .withStockOnHand(10)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(2))
            .withStockOnHand(20)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(3))
            .withStockOnHand(30)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(stockCard, lineItem);

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(2));

    assertThat(result.get(0).getStockOnHand(), is(10));
    assertThat(result.get(1).getStockOnHand(), is(25));
    assertThat(result.get(2).getStockOnHand(), is(35));
    assertThat(result.get(3).getStockOnHand(), is(45));
  }

  @Test
  public void shouldRecalculateStockOnHandWithDebitReason() {
    final StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withDebitReason()
        .withQuantity(5)
        .build();

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(1))
            .withStockOnHand(10)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(2))
            .withStockOnHand(20)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(3))
            .withStockOnHand(30)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(stockCard, lineItem);

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(2));

    assertThat(result.get(0).getStockOnHand(), is(10));
    assertThat(result.get(1).getStockOnHand(), is(5));
    assertThat(result.get(2).getStockOnHand(), is(15));
    assertThat(result.get(3).getStockOnHand(), is(25));
  }

  @Test
  public void shouldRecalculateStockOnHandForPhysicalInventory() {
    final StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withQuantity(50)
        .build();

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(1))
            .withStockOnHand(10)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(2))
            .withStockOnHand(20)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(3))
            .withStockOnHand(30)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(stockCard, lineItem);

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(2));

    assertThat(result.get(0).getStockOnHand(), is(10));
    assertThat(result.get(1).getStockOnHand(), is(50));
    assertThat(result.get(2).getStockOnHand(), is(60));
    assertThat(result.get(3).getStockOnHand(), is(70));
  }

  @Test
  public void shouldRecalculateExistingCalculatedStockOnHandForPhysicalInventory() {
    final StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withQuantity(50)
        .build();

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate())
            .withStockOnHand(10)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(2))
            .withStockOnHand(20)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(3))
            .withStockOnHand(30)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(stockCard, lineItem);

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate());

    assertThat(result.get(0).getStockOnHand(), is(50));
    assertThat(result.get(1).getStockOnHand(), is(60));
    assertThat(result.get(2).getStockOnHand(), is(70));
  }

  @Test
  public void shouldCreateNewCalculatedStockOnHandIfNoneExistsForStockCard() {
    final StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withCreditReason()
        .withQuantity(15)
        .build();

    calculatedStockOnHandService.recalculateStockOnHand(stockCard, lineItem);

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(2));

    assertThat(result.get(0).getStockOnHand(), is(15));
  }

  @Test
  public void shouldNotCreateNewCalculatedStockOnHandIfOneExistsForGivenDay() {
    final StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withCreditReason()
        .withQuantity(15)
        .build();

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate())
            .withStockOnHand(10)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(stockCard, lineItem);

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate());

    assertThat(result.get(0).getStockOnHand(), is(25));
  }
}
