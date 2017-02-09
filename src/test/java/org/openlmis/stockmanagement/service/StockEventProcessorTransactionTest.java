package org.openlmis.stockmanagement.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventProcessorTransactionTest extends BaseTest {

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private StockCardService stockCardService;

  @MockBean
  private StockEventValidationsService stockEventValidationsService;

  @Before
  public void setUp() throws Exception {
    doNothing().when(stockEventValidationsService).validate(any(StockEventDto.class));
  }

  @Test
  public void should_not_save_event_when_save_stock_card_failed() throws Exception {
    //given
    UserDto user = new UserDto();
    user.setId(UUID.randomUUID());
    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    //pretend that after event is saved, saving cards and line items will go wrong
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    Mockito.doThrow(new InstantiationException()).when(stockCardService)
        .saveFromEvent(any(StockEventDto.class), any(UUID.class), any(UUID.class));

    //when
    try {
      stockEventProcessor.process(stockEventDto);
    } catch (InstantiationException ex) {
      //then: the saved event should be rolled back
      assertThat(stockEventsRepository.findAll(), iterableWithSize(0));
      return;
    }

    Assert.fail();
  }
}