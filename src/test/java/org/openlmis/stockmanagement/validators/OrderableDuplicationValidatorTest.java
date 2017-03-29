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

import static org.junit.rules.ExpectedException.none;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ORDERABLE_DUPLICATION;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.util.ArrayList;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class OrderableDuplicationValidatorTest {

  @Rule
  public ExpectedException expectedEx = none();

  @InjectMocks
  private OrderableDuplicationValidator orderableDuplicationValidator;

  @Test
  public void stock_event_with_same_orderable_appear_twice_should_not_pass() throws Exception {
    //expect: exception
    expectedEx.expectMessage(ERROR_EVENT_ORDERABLE_DUPLICATION);

    //given: an event with orderable appear twice
    StockEventLineItem orderableLineItem1 = new StockEventLineItem();
    StockEventLineItem orderableLineItem2 = new StockEventLineItem();

    UUID orderableId = UUID.randomUUID();
    orderableLineItem1.setOrderableId(orderableId);
    orderableLineItem2.setOrderableId(orderableId);

    StockEventDto eventDto = new StockEventDto();
    eventDto.setLineItems(new ArrayList<>());
    eventDto.getLineItems().add(orderableLineItem1);
    eventDto.getLineItems().add(orderableLineItem2);

    //when
    orderableDuplicationValidator.validate(eventDto);
  }
}