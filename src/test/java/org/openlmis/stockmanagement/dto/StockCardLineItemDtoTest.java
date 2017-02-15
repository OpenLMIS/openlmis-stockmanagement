package org.openlmis.stockmanagement.dto;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StockCardLineItemDtoTest {

  @Test
  public void should_take_calculated_soh_from_line_item() throws Exception {
    //given
    StockCardLineItem mockedLineItem = mock(StockCardLineItem.class);
    when(mockedLineItem.calculateStockOnHand(100)).thenReturn(200);

    StockCardLineItemDto stockCardLineItemDto = StockCardLineItemDto.builder()
            .lineItem(mockedLineItem)
            .build();

    //when
    stockCardLineItemDto.calculateStockOnHand(100);

    //then
    assertThat(stockCardLineItemDto.getStockOnHand(), is(200));
  }
}