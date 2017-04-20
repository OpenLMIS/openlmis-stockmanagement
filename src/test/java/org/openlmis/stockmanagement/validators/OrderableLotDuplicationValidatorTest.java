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

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.junit.rules.ExpectedException.none;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_LOT_DUPLICATION;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.util.Arrays;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class OrderableLotDuplicationValidatorTest {

  @Rule
  public ExpectedException expectedEx = none();

  @InjectMocks
  private OrderableLotDuplicationValidator orderableLotDuplicationValidator;

  @Test
  public void adjustment_event_with_same_orderable_and_lot_appear_twice_should_pass()
      throws Exception {
    StockEventDto eventDto = createStockEventDtoWithDuplicateOrderableLot(randomUUID());

    //when
    orderableLotDuplicationValidator.validate(eventDto);
  }

  @Test
  public void physical_inventory_event_with_same_orderable_and_lot_appear_twice_should_not_pass()
      throws Exception {
    //expect: exception
    expectedEx.expectMessage(ERROR_EVENT_ORDERABLE_LOT_DUPLICATION);
    StockEventDto eventDto = createStockEventDtoWithDuplicateOrderableLot(null);

    //when
    orderableLotDuplicationValidator.validate(eventDto);
  }

  private StockEventDto createStockEventDtoWithDuplicateOrderableLot(UUID reasonId) {
    //given: an event with orderable appear twice
    StockEventLineItem eventLineItem1 = new StockEventLineItem();
    StockEventLineItem eventLineItem2 = new StockEventLineItem();
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