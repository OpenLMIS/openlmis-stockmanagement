package org.openlmis.stockmanagement.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventProcessorTest {

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @MockBean
  private StockEventValidationsService stockEventValidationsService;

  @MockBean
  private StockCardService stockCardService;

  @MockBean
  private StockEventsRepository stockEventsRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @Test
  public void should_not_save_events_without_user_permission() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();

    Mockito.doThrow(new MissingPermissionException(""))
            .when(stockEventValidationsService)
            .validate(stockEventDto);

    //when
    try {
      stockEventProcessor.process(stockEventDto);
    } catch (MissingPermissionException ex) {
      //then
      verify(stockEventsRepository, never()).save(any(StockEvent.class));
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_save_event_and_line_items_when_validation_service_passes() throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto();
    userDto.setId(userId);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    UUID eventIdFromRepo = UUID.randomUUID();
    StockEvent stockEvent = new StockEvent();
    stockEvent.setId(eventIdFromRepo);
    when(stockEventsRepository.save(any(StockEvent.class))).thenReturn(stockEvent);

    //when
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    UUID idFromProcessor = stockEventProcessor.process(stockEventDto);

    //then
    Class<List<StockCardLineItem>> clazz = (Class<List<StockCardLineItem>>) (Object) List.class;

    ArgumentCaptor<StockEvent> eventCaptor = ArgumentCaptor.forClass(StockEvent.class);
    ArgumentCaptor<List<StockCardLineItem>> lineItemCaptor = ArgumentCaptor.forClass(clazz);

    verify(stockEventsRepository).save(eventCaptor.capture());
    verify(stockCardService).save(lineItemCaptor.capture());

    assertThat(eventCaptor.getValue().getUserId(), is(userId));
    assertThat(lineItemCaptor.getValue().get(0).getUserId(), is(userId));

    assertThat(idFromProcessor, is(eventIdFromRepo));
  }
}