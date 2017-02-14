package org.openlmis.stockmanagement.domain.card;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEvent;
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
}