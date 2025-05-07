/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.stockmanagement.validators;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableChildDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventLineItemDtoDataBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.test.context.TestPropertySource;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource({"stockmanagement.kit.unpack.reasonId=a80d090b-56c3-4fa6-8403-a40da8323199"})
public class UnpackKitValidatorTest {

  private static final UUID UNPACK_REASON_ID = UUID
      .fromString("a80d090b-56c3-4fa6-8403-a40da8323199");

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private StockEventProcessContext context;

  @InjectMocks
  private DefaultUnpackKitValidator validator;


  private OrderableDto kitDto;
  private OrderableDto constituentDto1;
  private OrderableDto constituentDto2;

  @Before
  public void setup() {
    kitDto = OrderableDto.builder().id(UUID.randomUUID()).build();
    constituentDto1 = OrderableDto.builder().id(UUID.randomUUID())
        .children(Collections.emptySet())
        .build();
    constituentDto2 = OrderableDto.builder().id(UUID.randomUUID())
        .children(Collections.emptySet())
        .build();
    kitDto.setChildren(Stream.of(
        new OrderableChildDto(5, new ObjectReferenceDto(null, null, constituentDto1.getId())),
        new OrderableChildDto(10, new ObjectReferenceDto(null, null, constituentDto2.getId()))
    ).collect(Collectors.toSet()));
    List<OrderableDto> orderableDtos = asList(kitDto, constituentDto1, constituentDto2);
    when(orderableReferenceDataService.findByIds(any()))
        .thenReturn(orderableDtos);

    when(context.getUnpackReasonId())
        .thenReturn(UNPACK_REASON_ID);

  }

  @Test
  public void validateShouldNotRunForOrderablesThatAreNotKits() {
    StockEventDto dto = Mockito.mock(StockEventDto.class);
    when(dto.isKitUnpacking()).thenReturn(false);

    validator.validate(dto);
    verify(orderableReferenceDataService, never()).findByIds(any());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenUnpackReasonIsUsedForOrderableThatIsNotKit() {
    StockEventLineItemDto lineItemDto = createStockEventLineItem(UNPACK_REASON_ID,
        constituentDto1.getId(), 2);
    StockEventDto stockEventDto = createStockEvent(lineItemDto);

    validator.validate(stockEventDto);
  }

  @Test
  public void shouldNotThrowExceptionWhenUnpackingWithRightAmounts() {
    StockEventLineItemDto kitLineItem = createStockEventLineItem(UNPACK_REASON_ID,
        kitDto.getId(), 2);
    StockEventLineItemDto lineItem1 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto1.getId(), 10);

    StockEventLineItemDto lineItem2 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto2.getId(), 20);

    StockEventDto stockEventDto = createStockEvent(kitLineItem, lineItem1, lineItem2);

    validator.validate(stockEventDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenUnpackingWithLessQuantityThanUnpackList() {
    StockEventLineItemDto kitLineItem = createStockEventLineItem(UNPACK_REASON_ID,
        kitDto.getId(), 2);
    StockEventLineItemDto lineItem1 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto1.getId(), 5);

    StockEventLineItemDto lineItem2 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto2.getId(), 2);

    StockEventDto stockEventDto = createStockEvent(kitLineItem, lineItem1, lineItem2);

    validator.validate(stockEventDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenUnpackingWithMoreQuantityThanUnpackList() {
    StockEventLineItemDto kitLineItem = createStockEventLineItem(UNPACK_REASON_ID,
        kitDto.getId(), 2);
    StockEventLineItemDto lineItem1 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto1.getId(), 50);

    StockEventLineItemDto lineItem2 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto2.getId(), 20);

    StockEventDto stockEventDto = createStockEvent(kitLineItem, lineItem1, lineItem2);

    validator.validate(stockEventDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenUnpackingWithMissingConstituentLineItem() {
    StockEventLineItemDto kitLineItem = createStockEventLineItem(UNPACK_REASON_ID,
        kitDto.getId(), 2);
    StockEventLineItemDto lineItem1 = createStockEventLineItem(UUID.randomUUID(),
        constituentDto1.getId(), 10);

    StockEventDto stockEventDto = createStockEvent(kitLineItem, lineItem1);

    validator.validate(stockEventDto);
  }

  private StockEventLineItemDto createStockEventLineItem(UUID reasonId, UUID orderableId,
      int quantity) {
    return new StockEventLineItemDtoDataBuilder()
        .withReasonId(reasonId)
        .withOrderableId(orderableId)
        .withQuantity(quantity)
        .build();
  }

  private StockEventDto createStockEvent(StockEventLineItemDto... lineItemDto) {
    StockEventDto stockEventDto = new StockEventDtoDataBuilder()
        .addLineItem(lineItemDto)
        .build();

    stockEventDto.setContext(context);
    return stockEventDto;
  }
}
