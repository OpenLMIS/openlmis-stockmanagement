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

package org.openlmis.stockmanagement.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;

public class StockCardLineItemDtoTest {

  @Test
  public void createFromShouldExposeOriginEventIdAndType() {
    StockEvent event = new StockEventDataBuilder().withEventOrigin(EventOrigin.ISSUE).build();
    StockCardLineItem lineItem = new StockCardLineItemDataBuilder().withOriginEvent(event).build();

    StockCardLineItemDto dto = StockCardLineItemDto.createFrom(lineItem);

    assertThat(dto.getOriginEventId(), is(event.getId()));
    assertThat(dto.getEventOrigin(), is(EventOrigin.ISSUE));
  }

  @Test
  public void createFromShouldHandleNullOriginEvent() {
    StockCardLineItem lineItem = new StockCardLineItemDataBuilder().withOriginEvent(null).build();

    StockCardLineItemDto dto = StockCardLineItemDto.createFrom(lineItem);

    assertThat(dto.getOriginEventId(), is(nullValue()));
    assertThat(dto.getEventOrigin(), is(nullValue()));
  }
}
