package org.openlmis.stockmanagement.domain.card;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StockCardTest {
  @Test
  public void should_calculate_soh_as_zero_when_no_line_items() throws Exception {
    //given
    StockCard card = new StockCard();
    card.setLineItems(new ArrayList<>());

    //when
    Integer soh = card.calculateStockOnHand();

    //then
    assertThat(soh, is(0));
  }

  @Test
  public void should_calculate_soh_based_on_line_items() throws Exception {
    //given
    StockCardLineItem lineItem1 = mock(StockCardLineItem.class);
    StockCardLineItem lineItem2 = mock(StockCardLineItem.class);

    when(lineItem1.calculateStockOnHand(0)).thenReturn(123);
    when(lineItem2.calculateStockOnHand(123)).thenReturn(456);

    StockCard card = new StockCard();
    card.setLineItems(Arrays.asList(lineItem1, lineItem2));

    //when
    Integer soh = card.calculateStockOnHand();

    //then
    verify(lineItem1, Mockito.times(1)).calculateStockOnHand(0);
    verify(lineItem2, Mockito.times(1)).calculateStockOnHand(123);

    assertThat(soh, is(456));
  }
}