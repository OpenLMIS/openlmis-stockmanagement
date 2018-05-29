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

package org.openlmis.stockmanagement.domain.card;

import static java.lang.Integer.MAX_VALUE;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemFrom;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.util.LazyResource;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class StockCardLineItemTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldCreateLineItemFromStockEvent() throws Exception {
    //given
    StockCard stockCard = new StockCard();
    stockCard.setLineItems(new ArrayList<>());

    //when
    UUID userId = randomUUID();

    StockEventProcessContext context = new StockEventProcessContext();
    context.setCurrentUserId(new LazyResource<>(() -> userId));

    StockEventDto eventDto = createStockEventDto();
    eventDto.setContext(context);

    StockEventLineItemDto eventLineItem = eventDto.getLineItems().get(0);
    eventLineItem.setStockAdjustments(singletonList(createStockAdjustment()));

    UUID eventId = randomUUID();

    StockCardLineItem cardLineItem =
        createLineItemFrom(eventDto, eventLineItem, stockCard, eventId);

    //then
    assertThat(cardLineItem.getSourceFreeText(), is(eventLineItem.getSourceFreeText()));
    assertThat(cardLineItem.getDestinationFreeText(), is(eventLineItem.getDestinationFreeText()));
    assertThat(cardLineItem.getReasonFreeText(), is(eventLineItem.getReasonFreeText()));
    assertThat(cardLineItem.getDocumentNumber(), is(eventDto.getDocumentNumber()));
    assertThat(cardLineItem.getSignature(), is(eventDto.getSignature()));

    assertThat(cardLineItem.getQuantity(), is(eventLineItem.getQuantity()));
    assertThat(cardLineItem.getReason().getId(), is(eventLineItem.getReasonId()));

    assertThat(cardLineItem.getSource().getId(), is(eventLineItem.getSourceId()));
    assertThat(cardLineItem.getDestination().getId(), is(eventLineItem.getDestinationId()));

    assertThat(cardLineItem.getOccurredDate(), is(eventLineItem.getOccurredDate()));

    assertThat(cardLineItem.getStockCard(), is(stockCard));
    assertThat(cardLineItem.getOriginEvent().getId(), is(eventId));

    assertThat(cardLineItem.getUserId(), is(userId));

    assertEquals(cardLineItem.getStockAdjustments(), eventLineItem.stockAdjustments());

    ZonedDateTime processedDate = cardLineItem.getProcessedDate();
    long between = SECONDS.between(processedDate, ZonedDateTime.now());

    assertThat(between, lessThan(2L));
  }

  @Test
  public void shouldIncreaseSohOfLineItemWithCreditReason() throws Exception {
    //given
    StockCardLineItemReason creditReason = StockCardLineItemReason.builder()
        .reasonType(ReasonType.CREDIT).build();

    StockCardLineItem lineItem = StockCardLineItem.builder()
        .reason(creditReason)
        .quantity(10).build();
    //when
    lineItem.calculateStockOnHand(5);

    //then
    assertThat(lineItem.getStockOnHand(), is(15));
  }

  @Test
  public void shouldNotIncreaseSohOverIntLimit() throws Exception {
    //expect
    exception.expectMessage("exceed.upperLimit");

    //given
    StockCardLineItemReason creditReason = StockCardLineItemReason.builder()
        .reasonType(ReasonType.CREDIT).build();

    int quantityToAdd = 10;
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .reason(creditReason)
        .quantity(quantityToAdd).build();
    //when
    lineItem.calculateStockOnHand(MAX_VALUE - quantityToAdd + 1);
  }

  @Test
  public void shouldDecreaseSohOfLineItemWithDebitReason() throws Exception {
    //given
    StockCardLineItemReason debitReason = StockCardLineItemReason.builder()
        .reasonType(ReasonType.DEBIT).build();

    StockCardLineItem lineItem = StockCardLineItem.builder()
        .reason(debitReason)
        .quantity(5).build();
    //when
    lineItem.calculateStockOnHand(15);

    //then
    assertThat(lineItem.getStockOnHand(), is(10));
  }

  @Test
  public void shouldIncreaseSohOfLineItemWhenReceiveFrom() throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .source(new Node())
        .quantity(15).build();
    //when
    lineItem.calculateStockOnHand(15);

    //then
    assertThat(lineItem.getStockOnHand(), is(30));
  }

  @Test
  public void shouldDecreaseSohOfLineItemWhenIssueTo() throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .destination(new Node())
        .quantity(15).build();
    //when
    lineItem.calculateStockOnHand(15);

    //then
    assertThat(lineItem.getStockOnHand(), is(0));
  }

  @Test
  public void shouldAssignCreditReasonAndReturnQuantityAsSohForPhysicalOverstock()
      throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .quantity(15).build();

    //when
    lineItem.calculateStockOnHand(10);

    //then
    assertThat(lineItem.getStockOnHand(), is(15));
    assertThat(lineItem.getReason().getReasonType(), is(ReasonType.CREDIT));
    assertThat(lineItem.getReason().getReasonCategory(), is(ReasonCategory.PHYSICAL_INVENTORY));
  }

  @Test
  public void shouldAssignDebitReasonAndReturnQuantityAsSohForPhysicalUnderstock()
      throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .quantity(15).build();

    //when
    lineItem.calculateStockOnHand(20);

    //then
    assertThat(lineItem.getStockOnHand(), is(15));
    assertThat(lineItem.getReason().getReasonType(), is(ReasonType.DEBIT));
    assertThat(lineItem.getReason().getReasonCategory(), is(ReasonCategory.PHYSICAL_INVENTORY));
  }

  @Test
  public void shouldAssignBalanceReasonAndReturnQuantityAsSohForPhysicalBalance()
      throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .quantity(15).build();

    //when
    lineItem.calculateStockOnHand(15);

    //then
    assertThat(lineItem.getStockOnHand(), is(15));
    assertThat(lineItem.getReason().getReasonType(), is(ReasonType.BALANCE_ADJUSTMENT));
    assertThat(lineItem.getReason().getReasonCategory(), is(ReasonCategory.PHYSICAL_INVENTORY));
  }

  @Test
  public void shouldReturnNegativeValueForDebitReason() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .quantity(15)
        .reason(StockCardLineItemReason.physicalDebit())
        .build();

    assertThat(lineItem.getQuantityWithSign(), is(-15));
  }

  @Test
  public void shouldReturnPositiveValueForCreditReason() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .quantity(15)
        .reason(StockCardLineItemReason.physicalCredit())
        .build();

    assertThat(lineItem.getQuantityWithSign(), is(15));
  }

  @Test
  public void shouldReturnNullForEmptyQuantity() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .quantity(null)
        .reason(StockCardLineItemReason.physicalCredit())
        .build();

    assertNull(lineItem.getQuantityWithSign());
  }

  private static StockEventAdjustmentDto createStockAdjustment() {
    return new StockEventAdjustmentDto(UUID.randomUUID(), 10);
  }
}