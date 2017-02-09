package org.openlmis.stockmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.utils.Message;
import org.openlmis.stockmanagement.validators.StockEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventValidationsServiceTest {

  @Autowired
  private StockEventValidationsService stockEventValidationsService;

  @MockBean
  private PermissionService permissionService;

  @MockBean(name = "v1")
  private StockEventValidator validator1;

  @MockBean(name = "v2")
  private StockEventValidator validator2;

  @Test
  public void should_validate_current_user_permission() throws Exception {
    //given:
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();

    //when:
    stockEventValidationsService.validate(stockEventDto);

    //then:
    verify(permissionService, times(1))
        .canCreateStockEvent(stockEventDto.getProgramId(), stockEventDto.getFacilityId());

  }

  @Test
  public void should_validate_with_all_implementations_of_validators() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();

    //when:
    stockEventValidationsService.validate(stockEventDto);

    //then:
    verify(validator1, times(1)).validate(stockEventDto);
    verify(validator2, times(1)).validate(stockEventDto);
  }

  @Test
  public void should_not_run_next_validator_if_previous_validator_failed() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    doThrow(new ValidationMessageException(new Message("some error")))
        .when(validator1).validate(stockEventDto);

    //when:
    try {
      stockEventValidationsService.validate(stockEventDto);
    } catch (ValidationMessageException ex) {
      //then:
      verify(validator1, times(1)).validate(stockEventDto);
      verify(validator2, never()).validate(stockEventDto);
    }

  }
}