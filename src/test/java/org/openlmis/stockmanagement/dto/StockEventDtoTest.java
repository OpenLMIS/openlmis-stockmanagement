package org.openlmis.stockmanagement.dto;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class StockEventDtoTest {
  @Test
  public void should_convert_from_dto_to_jpa_model() throws Exception {
    //given
    StockEventDto stockEventDto = new StockEventDto();

    stockEventDto.setSourceFreeText("a");
    stockEventDto.setDestinationFreeText("b");
    stockEventDto.setDocumentNumber("c");
    stockEventDto.setReasonFreeText("d");
    stockEventDto.setSignature("e");

    stockEventDto.setQuantity(1);
    stockEventDto.setReasonId(UUID.randomUUID());

    stockEventDto.setSourceId(UUID.randomUUID());
    stockEventDto.setDestinationId(UUID.randomUUID());

    stockEventDto.setProgramId(UUID.randomUUID());
    stockEventDto.setFacilityId(UUID.randomUUID());
    stockEventDto.setOrderableId(UUID.randomUUID());

    stockEventDto.setStockCardId(UUID.randomUUID());

    stockEventDto.setNoticedDate(ZonedDateTime.now());
    stockEventDto.setOccurredDate(ZonedDateTime.now());

    //when
    UUID userId = UUID.randomUUID();
    StockEvent event = stockEventDto.toEvent(userId);

    //then
    assertThat(event.getSourceFreeText(), is(stockEventDto.getSourceFreeText()));
    assertThat(event.getDestinationFreeText(), is(stockEventDto.getDestinationFreeText()));
    assertThat(event.getDocumentNumber(), is(stockEventDto.getDocumentNumber()));
    assertThat(event.getReasonFreeText(), is(stockEventDto.getReasonFreeText()));
    assertThat(event.getSignature(), is(stockEventDto.getSignature()));

    assertThat(event.getQuantity(), is(stockEventDto.getQuantity()));
    assertThat(event.getReason().getId(), is(stockEventDto.getReasonId()));

    assertThat(event.getSource().getId(), is(stockEventDto.getSourceId()));
    assertThat(event.getDestination().getId(), is(stockEventDto.getDestinationId()));

    assertThat(event.getProgramId(), is(stockEventDto.getProgramId()));
    assertThat(event.getFacilityId(), is(stockEventDto.getFacilityId()));
    assertThat(event.getOrderableId(), is(stockEventDto.getOrderableId()));

    assertThat(event.getNoticedDate(), is(stockEventDto.getNoticedDate()));
    assertThat(event.getOccurredDate(), is(stockEventDto.getOccurredDate()));

    assertThat(event.getUserId(), is(userId));

    ZonedDateTime savedDate = event.getSavedDate();
    long between = SECONDS.between(savedDate, ZonedDateTime.now());

    assertThat(between, lessThan(2L));
  }
}