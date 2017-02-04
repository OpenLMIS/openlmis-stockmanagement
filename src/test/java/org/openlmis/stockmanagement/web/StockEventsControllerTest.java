package org.openlmis.stockmanagement.web;

import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.testutils.StockEventBuilder;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_EVENT_REASON_NOT_MATCH;
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
    UUID uuid = UUID.randomUUID();
    StockEventDto event = StockEventBuilder.createStockEventWithId(uuid);
    when(stockEventProcessor.process(event)).thenReturn(event);

    //when

    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(event)));

    //then
    resultActions
            .andExpect(status().isCreated())
            .andExpect(content().string("\"" + uuid.toString() + "\""));
  }

  @Test
  public void should_return_403_when_user_has_not_permission() throws Exception {
    //given
    Mockito.doThrow(new MissingPermissionException(""))
            .when(stockEventProcessor)
            .process(any());

    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(new StockEventDto())));

    //then
    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void should_return_400_when_validation_fails() throws Exception {
    //given
    Mockito.doThrow(new ValidationMessageException(new Message(ERROR_STOCK_EVENT_REASON_NOT_MATCH)))
            .when(stockEventProcessor)
            .process(any());

    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(new StockEventDto())));

    //then
    resultActions.andExpect(status().isBadRequest());
  }
}