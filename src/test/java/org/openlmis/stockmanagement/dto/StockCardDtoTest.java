package org.openlmis.stockmanagement.dto;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.testutils.DatesUtil;

import java.time.ZonedDateTime;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openlmis.stockmanagement.dto.StockCardLineItemDto.createFrom;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneDayLater;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneHourEarlier;
import static org.openlmis.stockmanagement.testutils.DatesUtil.oneHourLater;

public class StockCardDtoTest {
  @Test
  public void should_reorder_line_items_by_occurred_then_by_noticed() throws Exception {
    //given
    ZonedDateTime baseDate = DatesUtil.getBaseDateTime();

    StockCardLineItem lineItem1 = new StockCardLineItem();
    lineItem1.setOccurredDate(baseDate);
    lineItem1.setNoticedDate(oneDayLater(baseDate));

    StockCardLineItem lineItem2 = new StockCardLineItem();
    lineItem2.setOccurredDate(baseDate);
    lineItem2.setNoticedDate(oneHourLater(baseDate));

    StockCardLineItem lineItem3 = new StockCardLineItem();
    lineItem3.setOccurredDate(oneHourEarlier(baseDate));
    lineItem3.setNoticedDate(oneHourEarlier(baseDate));

    StockCardDto cardDto = StockCardDto.builder()
            .lineItems(asList(createFrom(lineItem1),
                    createFrom(lineItem2),
                    createFrom(lineItem3)))
            .build();

    //when
    cardDto.reorderLineItemsByDates();

    //then
    assertThat(cardDto.getLineItems().get(0).getLineItem(), is(lineItem3));
    assertThat(cardDto.getLineItems().get(1).getLineItem(), is(lineItem2));
    assertThat(cardDto.getLineItems().get(2).getLineItem(), is(lineItem1));
  }
}