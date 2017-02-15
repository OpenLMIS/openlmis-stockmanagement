package org.openlmis.stockmanagement.domain.card;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.adjustment.ReasonType;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemsFrom;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

public class StockCardLineItemTest {
  @Test
  public void should_create_line_item_from_stock_event() throws Exception {
    //given
    StockEvent event = new StockEvent();
    event.setId(UUID.randomUUID());

    StockCard stockCard = new StockCard();
    stockCard.setOriginEvent(event);
    stockCard.setLineItems(new ArrayList<>());

    //when
    StockEventDto eventDto = createStockEventDto();
    UUID userId = UUID.randomUUID();
    List<StockCardLineItem> lineItems =
            createLineItemsFrom(eventDto, stockCard, event.getId(), userId);
    StockCardLineItem lineItem = lineItems.get(0);

    //then
    assertThat(lineItem.getSourceFreeText(), is(eventDto.getSourceFreeText()));
    assertThat(lineItem.getDestinationFreeText(), is(eventDto.getDestinationFreeText()));
    assertThat(lineItem.getReasonFreeText(), is(eventDto.getReasonFreeText()));
    assertThat(lineItem.getDocumentNumber(), is(eventDto.getDocumentNumber()));
    assertThat(lineItem.getSignature(), is(eventDto.getSignature()));

    assertThat(lineItem.getQuantity(), is(eventDto.getQuantity()));
    assertThat(lineItem.getReason().getId(), is(eventDto.getReasonId()));

    assertThat(lineItem.getSource().getId(), is(eventDto.getSourceId()));
    assertThat(lineItem.getDestination().getId(), is(eventDto.getDestinationId()));

    assertThat(lineItem.getOccurredDate(), is(eventDto.getOccurredDate()));
    assertThat(lineItem.getNoticedDate(), is(eventDto.getNoticedDate()));

    assertThat(lineItem.getStockCard(), is(stockCard));
    assertThat(lineItem.getOriginEvent().getId(), is(event.getId()));

    assertThat(lineItem.getUserId(), is(userId));

    ZonedDateTime savedDate = lineItem.getSavedDate();
    long between = SECONDS.between(savedDate, ZonedDateTime.now());

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
    int soh = lineItem.calculateStockOnHand(5);

    //then
    assertThat(soh, is(15));
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
    int soh = lineItem.calculateStockOnHand(15);

    //then
    assertThat(soh, is(10));
  }

  @Test
  public void should_take_previous_soh_when_reason_is_balance_adjustment() throws Exception {
    //given
    StockCardLineItemReason balanceAdjustmentReason = StockCardLineItemReason.builder()
            .reasonType(ReasonType.BALANCE_ADJUSTMENT).build();

    StockCardLineItem lineItem = StockCardLineItem.builder()
            .reason(balanceAdjustmentReason)
            .quantity(15).build();
    //when
    int soh = lineItem.calculateStockOnHand(15);

    //then
    assertThat(soh, is(15));
  }

  @Test
  public void should_increase_soh_of_line_item_when_receive_from() throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
            .source(new Node())
            .quantity(15).build();
    //when
    int soh = lineItem.calculateStockOnHand(15);

    //then
    assertThat(soh, is(30));
  }

  @Test
  public void should_decrease_soh_of_line_item_when_issue_to() throws Exception {
    //given
    StockCardLineItem lineItem = StockCardLineItem.builder()
            .destination(new Node())
            .quantity(15).build();
    //when
    int soh = lineItem.calculateStockOnHand(15);

    //then
    assertThat(soh, is(0));
  }
}