package org.openlmis.stockmanagement.dto;

import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.dto.StockCardLineItemDto.createFrom;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDateTime;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneDayLater;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneHourEarlier;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneHourLater;

public class StockCardDtoTest {
  @Test
  public void should_reorder_line_items_by_occurred_then_by_noticed_when_calculate_soh()
          throws Exception {
    //given
    ZonedDateTime baseDate = getBaseDateTime();

    StockCardLineItem lineItem1 = new StockCardLineItem();
    lineItem1.setOccurredDate(baseDate);
    lineItem1.setNoticedDate(oneDayLater(baseDate));
    lineItem1.setQuantity(1);

    StockCardLineItem lineItem2 = new StockCardLineItem();
    lineItem2.setOccurredDate(baseDate);
    lineItem2.setNoticedDate(oneHourLater(baseDate));
    lineItem2.setQuantity(1);

    StockCardLineItem lineItem3 = new StockCardLineItem();
    lineItem3.setOccurredDate(oneHourEarlier(baseDate));
    lineItem3.setNoticedDate(oneHourEarlier(baseDate));
    lineItem3.setQuantity(1);

    StockCardDto cardDto = StockCardDto.builder()
            .lineItems(asList(
                    createFrom(lineItem1),
                    createFrom(lineItem2),
                    createFrom(lineItem3)))
            .build();

    //when
    cardDto.calculateStockOnHand();

    //then
    assertThat(cardDto.getLineItems().get(0).getLineItem(), is(lineItem3));
    assertThat(cardDto.getLineItems().get(1).getLineItem(), is(lineItem2));
    assertThat(cardDto.getLineItems().get(2).getLineItem(), is(lineItem1));
  }

  @Test
  public void should_get_soh_by_calculating_soh_of_each_line_item_dto() throws Exception {
    //given
    StockCardLineItem lineItem1 = new StockCardLineItem();
    StockCardLineItem lineItem2 = new StockCardLineItem();

    lineItem1.setOccurredDate(getBaseDateTime());
    lineItem2.setOccurredDate(oneDayLater(getBaseDateTime()));

    StockCardLineItemDto lineItemDto1 = mock(StockCardLineItemDto.class);
    StockCardLineItemDto lineItemDto2 = mock(StockCardLineItemDto.class);

    when(lineItemDto1.getStockOnHand()).thenReturn(123);
    when(lineItemDto2.getStockOnHand()).thenReturn(456);

    when(lineItemDto1.getLineItem()).thenReturn(lineItem1);
    when(lineItemDto2.getLineItem()).thenReturn(lineItem2);

    List<StockCardLineItemDto> lineItemDtos = Arrays.asList(lineItemDto1, lineItemDto2);

    StockCardDto cardDto = StockCardDto.builder()
            .lineItems(lineItemDtos)
            .build();

    //when
    cardDto.calculateStockOnHand();

    //then
    verify(lineItemDto1, Mockito.times(1)).calculateStockOnHand(0);
    verify(lineItemDto2, Mockito.times(1)).calculateStockOnHand(123);

    assertThat(cardDto.getStockOnHand(), is(456));
  }
}