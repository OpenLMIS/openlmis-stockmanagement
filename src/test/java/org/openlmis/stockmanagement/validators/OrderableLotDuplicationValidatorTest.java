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

import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_LOT_DUPLICATION;

import java.util.Arrays;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;

@RunWith(MockitoJUnitRunner.class)
public class OrderableLotDuplicationValidatorTest extends BaseValidatorTest {

  @Rule
  public ExpectedException expectedEx = none();

  @InjectMocks
  private OrderableLotDuplicationValidator orderableLotDuplicationValidator;

  @Test
  public void adjustmentEventWithSameOrderableAndLotAppearTwiceShouldPass()
      throws Exception {
    StockEventDto eventDto = createStockEventDtoWithDuplicateOrderableLot(randomUUID());

    //when
    orderableLotDuplicationValidator.validate(eventDto);
  }

  @Test
  public void physicalInventoryEventWithSameOrderableAndLotAppearTwiceShouldNotPass()
      throws Exception {
    //expect: exception
    expectedEx.expectMessage(ERROR_EVENT_ORDERABLE_LOT_DUPLICATION + ": C100 Levora / LOT-1");
    StockEventDto eventDto = createStockEventDtoWithDuplicateOrderableLot(null);
    prepareContext(eventDto);

    //when
    orderableLotDuplicationValidator.validate(eventDto);
  }

  @Test
  public void physicalInventoryEventWithSameOrderableWithoutLotAppearTwiceShouldNotPass()
      throws Exception {
    //expect: exception
    expectedEx.expectMessage(ERROR_EVENT_ORDERABLE_LOT_DUPLICATION + ": C100 Levora");
    StockEventDto eventDto = createStockEventDtoWithDuplicateOrderableLot(null);
    eventDto.getLineItems().forEach(lineItem -> lineItem.setLotId(null));
    prepareContext(eventDto);

    //when
    orderableLotDuplicationValidator.validate(eventDto);
  }

  private void prepareContext(StockEventDto eventDto) {
    StockEventLineItemDto lineItem = eventDto.getLineItems().get(0);
    OrderableDto orderableDto = OrderableDto.builder()
        .id(lineItem.getOrderableId())
        .productCode("C100")
        .fullProductName("Levora")
        .build();
    when(orderableReferenceDataService.findByIds(any())).thenReturn(singletonList(orderableDto));

    if (lineItem.getLotId() != null) {
      LotDto lotDto = new LotDto();
      lotDto.setId(lineItem.getLotId());
      lotDto.setLotCode("LOT-1");
      when(lotReferenceDataService.findOne(lineItem.getLotId())).thenReturn(lotDto);
    }

    setContext(eventDto);
  }

  private StockEventDto createStockEventDtoWithDuplicateOrderableLot(UUID reasonId) {
    //given: an event with orderable appear twice
    StockEventLineItemDto eventLineItem1 = new StockEventLineItemDto();
    StockEventLineItemDto eventLineItem2 = new StockEventLineItemDto();
    eventLineItem1.setReasonId(reasonId);
    eventLineItem2.setReasonId(reasonId);

    UUID orderableId = randomUUID();
    UUID lotId = randomUUID();

    eventLineItem1.setOrderableId(orderableId);
    //same uuid string, different object, make sure the code recognize them as same uuid
    eventLineItem2.setOrderableId(fromString(orderableId.toString()));

    eventLineItem1.setLotId(lotId);
    eventLineItem2.setLotId(fromString(lotId.toString()));

    StockEventDto eventDto = new StockEventDto();
    eventDto.setLineItems(Arrays.asList(eventLineItem1, eventLineItem2));
    return eventDto;
  }
}