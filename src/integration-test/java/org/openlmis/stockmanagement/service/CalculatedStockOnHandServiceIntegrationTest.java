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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

  private int quantity = new Random().nextInt();
  private int quantity2 = new Random().nextInt();

  private StockCard stockCard;

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
  }

  @Test
  public void shouldGetLatestStockCardsWithStockOnHandWhenDateIsNotSet() {
    LocalDate today = LocalDate.now();
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHand(
        quantity, stockCard, today, ZonedDateTime.now());

    calculatedStockOnHandRepository.save(calculatedStockOnHand);

    List<StockCard> foundStockCards = calculatedStockOnHandService
        .getStockCardsWithStockOnHand(program, facility);

    assertThat(foundStockCards.size(), is(1));
    assertThat(foundStockCards.get(0).getStockOnHand(), is(quantity));
    assertThat(foundStockCards.get(0).getOccurredDate(), is(today));
  }

  @Test
  public void shouldGetStockCardsWithStockOnHandWithSpecificDate() {
    LocalDate newDate = LocalDate.of(2018, 5, 10);
    LocalDate oldDate = LocalDate.of(2015, 5, 10);
    
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHand(quantity,
        stockCard, oldDate, ZonedDateTime.now());
    CalculatedStockOnHand calculatedStockOnHand2 = new CalculatedStockOnHand(quantity2,
        stockCard, newDate, ZonedDateTime.now());

    calculatedStockOnHandRepository.save(calculatedStockOnHand);
    calculatedStockOnHandRepository.save(calculatedStockOnHand2);

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
    
    CalculatedStockOnHand calculatedStockOnHand = new CalculatedStockOnHand(quantity,
        stockCard, newDate, ZonedDateTime.now());
    CalculatedStockOnHand calculatedStockOnHand2 = new CalculatedStockOnHand(quantity2,
        stockCard, oldDate, ZonedDateTime.now());

    calculatedStockOnHandRepository.save(calculatedStockOnHand);
    calculatedStockOnHandRepository.save(calculatedStockOnHand2);

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
    CalculatedStockOnHand calculatedStockOnHand2 = new CalculatedStockOnHand(quantity,
        stockCard, LocalDate.now(), ZonedDateTime.now());
    
    calculatedStockOnHandRepository.save(calculatedStockOnHand2);
    
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, LocalDate.now());

    assertThat(stockCard.getStockOnHand(), is(quantity));
  }
}
