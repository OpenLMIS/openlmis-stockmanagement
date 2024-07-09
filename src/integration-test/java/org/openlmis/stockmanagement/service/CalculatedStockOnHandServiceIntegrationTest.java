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
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@SuppressWarnings("PMD.TooManyMethods")
public class CalculatedStockOnHandServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private CalculatedStockOnHandService calculatedStockOnHandService;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockCardLineItemRepository stockCardLineItemRepository;

  @Autowired
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  private UUID facility = randomUUID();
  private UUID program = randomUUID();
  private UUID product = randomUUID();
  private UUID lot = randomUUID();

  private int quantity = 100;
  private int quantity2 = 200;

  private StockEvent event;
  private StockCard stockCard;
  private List<StockCardLineItem> lineItemlist;
  private StockCardLineItemReason creditReason;
  private StockCardLineItemReason debitReason;
  private CalculatedStockOnHandDataBuilder calculatedStockOnHandDataBuilder;

  @Before
  public void setUp() {
    when(orderableReferenceDataService.findOne(product)).thenReturn(
        OrderableDto.builder()
            .id(product)
            .productCode("TEST")
            .build());

    event = prepareEvent(facility, program);
    stockCard = prepareStockCard(event, product, lot);

    creditReason = stockCardLineItemReasonRepository.findByName("Transfer In");
    debitReason = stockCardLineItemReasonRepository.findByName("Consumed");

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
        .getStockCardsWithStockOnHand(program, facility, LocalDate.of(2017, 5, 10));

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
    calculatedStockOnHandService.fetchCurrentStockOnHand(stockCard);

    assertThat(stockCard.getStockOnHand(), is(0));
  }

  @Test
  public void shouldSetStockOnHandForStockCard() {
    calculatedStockOnHandRepository.save(calculatedStockOnHandDataBuilder.build());
    
    calculatedStockOnHandService.fetchCurrentStockOnHand(stockCard);

    assertThat(stockCard.getStockOnHand(), is(quantity));
  }

  @Test
  public void shouldRecalculateStockOnHandWithCreditReason() {
    final StockCardLineItem lineItem = createBaseLineItem(15, creditReason);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

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

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

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
    final StockCardLineItem lineItem = createBaseLineItem(5, debitReason);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

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

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

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
    final StockCardLineItem lineItem = createBaseLineItem(50);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

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

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

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
    final int physicalInventoryQuantity = 50;
    final int firstDaySoh = 10;
    final int calculatedFirstDaySoh = 50;
    final int secondDaySoh = 20;
    final int calculatedSecondDaySoh = 60;
    final int thirdDaySoh = 30;
    final int calculatedThirdDaySoh = 70;

    final StockCardLineItem lineItem = createBaseLineItem(physicalInventoryQuantity);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate())
            .withStockOnHand(firstDaySoh)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(2))
            .withStockOnHand(secondDaySoh)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(3))
            .withStockOnHand(thirdDaySoh)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate());

    /**
     * We are submitting physical inventory for day 1 with {@link physicalInventoryQuantity},
     * current SoH for day 1 is {@link firstDaySoh}.
     * After recalculation, it should be changed to {@link calculatedFirstDaySoh}.
     * From those 2 numbers, we can calculate that the difference between old SoH
     * and submitted physical inventory is +40.
     * Then we need to update all following days with the difference calculated,
     * so previously day 2 had SoH {@link secondDaySoh}, now it is {@link calculatedSecondDaySoh},
     * and day 3 had SoH {@link thirdDaySoh}, now it has {@link calculatedThirdDaySoh}.
     */
    assertThat(result.get(0).getStockOnHand(), is(calculatedFirstDaySoh));
    assertThat(result.get(1).getStockOnHand(), is(calculatedSecondDaySoh));
    assertThat(result.get(2).getStockOnHand(), is(calculatedThirdDaySoh));
  }

  @Test
  public void shouldCreateNewCalculatedStockOnHandIfNoneExistsForStockCard() {
    final StockCardLineItem lineItem = createBaseLineItem(15, creditReason);

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(2));

    assertThat(result.get(0).getStockOnHand(), is(15));
  }

  @Test
  public void shouldNotCreateNewCalculatedStockOnHandIfOneExistsForGivenDay() {
    final StockCardLineItem lineItem = createBaseLineItem(15, creditReason);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(1))
            .withStockOnHand(10)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate());

    assertThat(result.get(0).getStockOnHand(), is(25));
  }

  @Test
  public void shouldNotCreateCalculatedSohWhenAddingPriorAdjustmentIfAnySohExistsForGivenDay() {
    final StockCardLineItem lineItem = createBaseLineItem(15);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    lineItemlist.get(0).setOccurredDate(lineItem.getOccurredDate().plusDays(1));
    lineItemlist.get(0).setProcessedDate(lineItem.getProcessedDate().plusHours(1));
    lineItemlist.get(1).setOccurredDate(lineItem.getOccurredDate().plusDays(1));
    lineItemlist.get(1).setProcessedDate(lineItem.getProcessedDate().plusHours(2));
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(1))
            .withStockOnHand(10)
            .build());

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(1))
            .withStockOnHand(20)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate());

    assertThat(result.get(0).getStockOnHand(), is(15));
    assertThat(result.get(1).getStockOnHand(), is(35));
  }

  @Test
  public void shouldSetPhysicalInventoryValueAsStockOnHandWhenPhysicalInventoryIsTheLastValue() {
    final StockCardLineItem lineItem = createBaseLineItem(150);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(3))
            .withStockOnHand(10)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(2))
            .withStockOnHand(20)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(1))
            .withStockOnHand(30)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(3));

    assertThat(result.get(0).getStockOnHand(), is(10));
    assertThat(result.get(1).getStockOnHand(), is(20));
    assertThat(result.get(2).getStockOnHand(), is(30));
    assertThat(result.get(3).getStockOnHand(), is(150));
  }

  @Test
  public void shouldSetProperSohForAdjustmentsOnFollowingDaysForTheSameProduct() {
    LocalDate firstDate = LocalDate.of(2020, 2, 3);
    int initialSoh = 80;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        firstDate.minusDays(1), ZonedDateTime.now()));

    lineItemlist = Arrays.asList(
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate)),
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate.plusDays(1)))
    );
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(lineItemlist);

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate.plusDays(1));
    assertThat(stockCard.getStockOnHand(), is(78));
  }

  @Test
  public void shouldSetProperSohForAdjustmentsOnFollowingDaysForTheSameProductDoneTwice() {
    LocalDate firstDate = LocalDate.of(2010, 2, 14);
    int initialSoh = 64;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        firstDate.minusDays(1), ZonedDateTime.now()));

    List<StockCardLineItem> firstLineItemsList = Arrays.asList(
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate)),
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate.plusDays(1)))
    );
    stockCard.setLineItems(firstLineItemsList);
    calculatedStockOnHandService.recalculateStockOnHand(firstLineItemsList);

    List<StockCardLineItem> secondLineItemsList = Arrays.asList(
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate)),
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate.plusDays(1)))
    );
    lineItemlist = Stream.concat(firstLineItemsList.stream(), secondLineItemsList.stream())
        .collect(Collectors.toList());
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(secondLineItemsList);

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate.plusDays(1));
    assertThat(stockCard.getStockOnHand(), is(60));
  }

  @Test
  public void shouldSetProperSohForAllAdjustmentsAfterUpdatingTheValueForPreviousEvents() {
    LocalDate firstDate = LocalDate.of(2019, 5, 3);
    int initialSoh = 50;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        firstDate.minusDays(1), ZonedDateTime.now()));

    List<StockCardLineItem> firstEventItems = Collections.singletonList(
        stockCardLineItemRepository.save(createDebitLineItem(5, firstDate))
    );
    stockCard.setLineItems(firstEventItems);
    calculatedStockOnHandService.recalculateStockOnHand(firstEventItems);

    List<StockCardLineItem> secondEventItems = Collections.singletonList(
        stockCardLineItemRepository.save(createDebitLineItem(10, firstDate.plusDays(2)))
    );
    lineItemlist = Stream.concat(firstEventItems.stream(), secondEventItems.stream())
        .collect(Collectors.toList());
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(secondEventItems);

    List<StockCardLineItem> thirdEventItems = Arrays.asList(
        stockCardLineItemRepository.save(createDebitLineItem(1, firstDate)),
        stockCardLineItemRepository.save(createDebitLineItem(2, firstDate.plusDays(1)))
    );
    lineItemlist = Stream.concat(lineItemlist.stream(), thirdEventItems.stream())
        .collect(Collectors.toList());
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(thirdEventItems);

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate);
    assertThat(stockCard.getStockOnHand(), is(44));
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate.plusDays(1));
    assertThat(stockCard.getStockOnHand(), is(42));
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate.plusDays(2));
    assertThat(stockCard.getStockOnHand(), is(32));
  }

  @Test
  public void shouldPutAdjustmentsAsLastLineItemForASpecificDate() {
    LocalDate firstDate = LocalDate.of(2018, 3, 3);
    int initialSoh = 70;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        firstDate.minusDays(1), ZonedDateTime.now()));

    List<StockCardLineItem> firstEventItems = Collections.singletonList(
        stockCardLineItemRepository.save(createDebitLineItem(60, firstDate))
    );
    stockCard.setLineItems(firstEventItems);
    calculatedStockOnHandService.recalculateStockOnHand(firstEventItems);

    List<StockCardLineItem> secondEventItems = Collections.singletonList(
        stockCardLineItemRepository.save(createPhysicalInventory(40, firstDate))
    );
    lineItemlist = Stream.concat(firstEventItems.stream(), secondEventItems.stream())
        .collect(Collectors.toList());
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(secondEventItems);

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate);
    assertThat(stockCard.getStockOnHand(), is(40));
  }

  @Test
  public void shouldSetProperSohForAdjustmentsOnFollowingDaysForTheSameProductAddedInInvOrder() {
    LocalDate firstDate = LocalDate.of(2016, 1, 31);
    int initialSoh = 14;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        firstDate.minusDays(1), ZonedDateTime.now()));

    lineItemlist = Arrays.asList(
        stockCardLineItemRepository.save(createDebitLineItem(2, firstDate.plusDays(1))),
        stockCardLineItemRepository.save(createDebitLineItem(2, firstDate))
    );
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(lineItemlist);

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, firstDate.plusDays(1));
    assertThat(stockCard.getStockOnHand(), is(10));
  }

  @Test
  public void shouldSetProperSohForAdjustmentsOnTheSameDayForTheSameProduct() {
    LocalDate date = LocalDate.of(2018, 4, 15);
    int initialSoh = 60;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        date.minusDays(1), ZonedDateTime.now()));

    lineItemlist = Arrays.asList(
        stockCardLineItemRepository.save(createDebitLineItem(1, date)),
        stockCardLineItemRepository.save(createDebitLineItem(2, date))
    );
    stockCard.setLineItems(lineItemlist);
    calculatedStockOnHandService.recalculateStockOnHand(lineItemlist);

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, date);
    assertThat(stockCard.getStockOnHand(), is(57));
  }

  @Test
  public void shouldSetProperSohForAdjustmentsOnTheSameDayForTheDifferentStockCard() {
    LocalDate date = LocalDate.of(2016, 4, 15);
    int initialSoh = 40;

    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, stockCard,
        date.minusDays(1), ZonedDateTime.now()));
    StockCardLineItem baseLineItem = stockCardLineItemRepository
        .save(createDebitLineItem(1, date, stockCard));
    stockCardRepository.save(stockCard);
    stockCard.setLineItems(Collections.singletonList(baseLineItem));

    StockEvent alternateEvent = prepareEvent(randomUUID(), randomUUID());
    StockCard alternateStockCard = prepareStockCard(alternateEvent, randomUUID(), randomUUID());
    calculatedStockOnHandRepository.save(new CalculatedStockOnHand(initialSoh, alternateStockCard,
        date.minusDays(1), ZonedDateTime.now()));
    StockCardLineItem alternateLineItem = stockCardLineItemRepository
        .save(createDebitLineItem(3, date, alternateStockCard));
    stockCardRepository.save(alternateStockCard);
    alternateStockCard.setLineItems(Collections.singletonList(alternateLineItem));

    calculatedStockOnHandService.recalculateStockOnHand(
        Arrays.asList(baseLineItem, alternateLineItem));

    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(stockCard, date);
    calculatedStockOnHandService.fetchStockOnHandForSpecificDate(alternateStockCard, date);

    assertThat(stockCard.getStockOnHand(), is(39));
    assertThat(alternateStockCard.getStockOnHand(), is(37));
  }

  @Test
  public void shouldSetProperSohWhenAddingAdjustmentBeforePhysicalInventoryAsTheLastValue() {
    final StockCardLineItem lineItem = createBaseLineItem(15, creditReason);

    lineItemlist = createStockCardLineItemsList(lineItem, null);
    lineItemlist.get(0).setOccurredDate(lineItem.getOccurredDate().minusDays(3));
    lineItemlist.get(1).setOccurredDate(lineItem.getOccurredDate().minusDays(2));
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(3))
            .withStockOnHand(10)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().minusDays(2))
            .withStockOnHand(20)
            .build());
    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(1))
            .withStockOnHand(10)
            .build());

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(3));

    assertThat(result.get(0).getStockOnHand(), is(10));
    assertThat(result.get(1).getStockOnHand(), is(20));
    assertThat(result.get(2).getStockOnHand(), is(35));
    assertThat(result.get(3).getStockOnHand(), is(10));
  }

  @Test
  public void shouldRecalculateStockOnHandAfterAddingLineItemWithPreviousOccurredDate() {
    final StockCardLineItem lineItem = createBaseLineItem(15);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    lineItemlist.get(0).setOccurredDate(lineItem.getOccurredDate().plusDays(1));
    stockCard.setLineItems(lineItemlist);
    stockCardRepository.save(stockCard);

    calculatedStockOnHandRepository.save(
        calculatedStockOnHandDataBuilder
            .withOccurredDate(lineItem.getOccurredDate().plusDays(1))
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

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));

    List<CalculatedStockOnHand> result = calculatedStockOnHandRepository
        .findByStockCardIdAndOccurredDateGreaterThanEqualOrderByOccurredDateAsc(
            stockCard.getId(),
            lineItem.getOccurredDate().minusDays(3));

    assertThat(result.get(0).getStockOnHand(), is(15));
    assertThat(result.get(1).getStockOnHand(), is(25));
    assertThat(result.get(2).getStockOnHand(), is(35));
    assertThat(result.get(3).getStockOnHand(), is(45));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenQuantityMakesStockOnHandBelowZero() {
    final StockCardLineItem lineItem = createBaseLineItem(15, debitReason);

    lineItemlist = createStockCardLineItemsList(lineItem, creditReason);
    stockCard.setLineItems(lineItemlist);
    lineItem.setStockCard(stockCard);
    stockCardRepository.save(stockCard);

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

    calculatedStockOnHandService.recalculateStockOnHand(Collections.singletonList(lineItem));
  }

  private StockEvent prepareEvent(UUID facility, UUID program) {
    StockEvent result = new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .build();
    return stockEventsRepository.save(result);
  }

  private StockCard prepareStockCard(StockEvent event, UUID product, UUID lot) {
    StockCard result = new StockCardDataBuilder(event)
        .withoutId()
        .withOrderableId(product)
        .withLotId(lot)
        .build();
    return stockCardRepository.save(result);
  }

  private StockCardLineItem createPhysicalInventory(int quantity, LocalDate occuredDate) {
    return createLineItem(quantity, occuredDate, stockCard, null);
  }

  private StockCardLineItem createDebitLineItem(int quantity, LocalDate occuredDate) {
    return createDebitLineItem(quantity, occuredDate, stockCard);
  }

  private StockCardLineItem createDebitLineItem(int quantity, LocalDate occuredDate,
      StockCard card) {
    return createLineItem(quantity, occuredDate, card, debitReason);
  }

  private StockCardLineItem createBaseLineItem(int quantity) {
    return createBaseLineItem(quantity, null);
  }

  private StockCardLineItem createBaseLineItem(int quantity, StockCardLineItemReason reason) {
    return createLineItem(quantity, getBaseDate(), stockCard, reason);
  }

  private StockCardLineItem createLineItem(int quantity, LocalDate occuredDate,
      StockCard stockCard, StockCardLineItemReason reason) {
    return new StockCardLineItemDataBuilder()
        .withReason(reason)
        .withOccurredDate(occuredDate)
        .withOriginEvent(event)
        .withQuantity(quantity)
        .withStockCard(stockCard)
        .build();
  }

  private List<StockCardLineItem> createStockCardLineItemsList(StockCardLineItem lineItem,
      StockCardLineItemReason reason) {

    StockCardLineItem lineItem1 = new StockCardLineItemDataBuilder()
        .withReason(reason)
        .withOccurredDate(lineItem.getOccurredDate().minusDays(1))
        .withQuantity(10)
        .withOriginEvent(event)
        .build();
    lineItem1.setStockCard(stockCard);
    stockCardLineItemRepository.save(lineItem1);

    StockCardLineItem lineItem2 = new StockCardLineItemDataBuilder()
        .withReason(reason)
        .withOccurredDate(lineItem.getOccurredDate().plusDays(2))
        .withQuantity(10)
        .withOriginEvent(event)
        .build();
    lineItem2.setStockCard(stockCard);
    stockCardLineItemRepository.save(lineItem2);

    StockCardLineItem lineItem3 = new StockCardLineItemDataBuilder()
        .withReason(reason)
        .withOccurredDate(lineItem.getOccurredDate().plusDays(3))
        .withQuantity(10)
        .withOriginEvent(event)
        .build();
    lineItem3.setStockCard(stockCard);
    stockCardLineItemRepository.save(lineItem3);

    List<StockCardLineItem> list = new ArrayList<>();
    list.add(lineItem1);
    list.add(lineItem2);
    list.add(lineItem3);

    return list;
  }
}
