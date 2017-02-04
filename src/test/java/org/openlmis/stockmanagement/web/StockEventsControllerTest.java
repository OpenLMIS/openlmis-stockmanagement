package org.openlmis.stockmanagement.web;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.testutils.StockEventBuilder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StockEventsControllerTest extends BaseWebTest {

  private static final String CREATE_STOCK_EVENT_API = "/api/stockEvents";

  @MockBean
  private StockEventProcessor stockEventProcessor;

  @Test
  public void should_return_201_when_event_successfully_created() throws Exception {
    //given
    StockEvent event = StockEventBuilder.createStockEventWithQuantity(123);
    when(stockEventProcessor.process(event)).thenReturn(event);

    //when

    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(event)));

    //then
    resultActions
            .andExpect(status().isCreated())
            .andExpect(content().json("{'quantity':123}"));
  }
}