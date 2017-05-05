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

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemFrom;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class StockCardLineItemTest {

  @Test
  public void should_create_line_item_from_stock_event() throws Exception {
    //given
    StockCard stockCard = new StockCard();
    stockCard.setLineItems(new ArrayList<>());

    //when
    StockEventDto eventDto = createStockEventDto();
    StockEventLineItem eventLineItem = eventDto.getLineItems().get(0);

    UUID userId = randomUUID();
    UUID eventId = randomUUID();

    StockCardLineItem cardLineItem =
        createLineItemFrom(eventDto, eventLineItem, stockCard, eventId, userId);

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

    ZonedDateTime processedDate = cardLineItem.getProcessedDate();
    long between = SECONDS.between(processedDate, ZonedDateTime.now());

    assertThat(between, lessThan(2L));
  }

  @Test
  public void should_increase_soh_of_line_item_with_credit_reason() throws Exception {
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
  public void should_decrease_soh_of_line_item_with_debit_reason() throws Exception {
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
  public void should_increase_soh_of_line_item_when_receive_from() throws Exception {
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
  public void should_decrease_soh_of_line_item_when_issue_to() throws Exception {
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
  public void should_assign_credit_reason_and_return_quantity_as_soh_for_physical_overstock()
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
  public void should_assign_debit_reason_and_return_quantity_as_soh_for_physical_understock()
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
  public void should_assign_balance_reason_and_return_quantity_as_soh_for_physical_balance()
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
}