package org.openlmis.stockmanagement.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemsRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
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
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventProcessorTest extends BaseTest {

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private StockEventValidationsService stockEventValidationsService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockCardLineItemsRepository stockCardLineItemsRepository;

  @After
  public void tearDown() throws Exception {
    stockCardLineItemsRepository.deleteAll();
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
  }

  @Test
  public void should_not_save_events_if_anything_goes_wrong_in_validations_service()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();

    Mockito.doThrow(new RuntimeException("something wrong from validations service"))
        .when(stockEventValidationsService)
        .validate(stockEventDto);

    //when
    try {
      stockEventProcessor.process(stockEventDto);
    } catch (RuntimeException ex) {
      //then
      assertEventAndCardAndLineItemTableSize(0);
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

    //when
    assertEventAndCardAndLineItemTableSize(0);

    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventProcessor.process(stockEventDto);

    //then
    assertEventAndCardAndLineItemTableSize(1);
  }

  private void assertEventAndCardAndLineItemTableSize(int size) {
    assertThat(stockEventsRepository.findAll(), iterableWithSize(size));
    assertThat(stockCardLineItemsRepository.findAll(), iterableWithSize(size));
    assertThat(stockCardRepository.findAll(), iterableWithSize(size));
  }
}