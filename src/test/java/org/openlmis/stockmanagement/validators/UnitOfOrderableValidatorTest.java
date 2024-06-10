package org.openlmis.stockmanagement.validators;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_UNIT_OF_ORDERABLE_DOES_NOT_EXIST;

import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.UnitOfOrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.UnitOfOrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;

public class UnitOfOrderableValidatorTest {

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private UnitOfOrderableReferenceDataService unitOfOrderableReferenceDataService;

  @InjectMocks
  private UnitOfOrderableValidator unitOfOrderableValidator;

  @Test
  public void shouldPassWhenUUIDUnitOfOrderableExist() {
    //given
    UUID unitOfOrderableId = UUID.randomUUID();

    when(unitOfOrderableReferenceDataService.findOne(unitOfOrderableId))
        .thenReturn(mock(UnitOfOrderableDto.class));

    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDtoWithTwoLineItems();
    eventDto.getLineItems().forEach(
        lineItemDto -> lineItemDto.setUnitOfOrderableId(unitOfOrderableId)
    );

    //when
    unitOfOrderableValidator.validate(eventDto);

    //then
    // no exception - ok
  }

  @Test
  public void shouldNotPassWhenUUIDUnitOfOrderableDoesNotExist() {
    //given
    UUID unitOfOrderableId = UUID.randomUUID();

    when(unitOfOrderableReferenceDataService.findOne(unitOfOrderableId))
        .thenReturn(null);

    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDtoWithTwoLineItems();
    eventDto.getLineItems().forEach(
        lineItemDto -> lineItemDto.setUnitOfOrderableId(unitOfOrderableId)
    );

    //expect: exception
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        containsString(ERROR_LINE_ITEM_UNIT_OF_ORDERABLE_DOES_NOT_EXIST)
    );

    //when-then
    unitOfOrderableValidator.validate(eventDto);
  }
}
