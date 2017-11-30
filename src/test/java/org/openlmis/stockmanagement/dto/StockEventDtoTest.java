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

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.util.LazyResource;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StockEventDtoTest {
  @Test
  public void shouldConvertFromDtoToJpaModel() throws Exception {
    //given
    UUID userId = UUID.randomUUID();

    StockEventProcessContext context = new StockEventProcessContext();
    context.setCurrentUserId(new LazyResource<>(() -> userId));

    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.setContext(context);

    //when
    StockEvent event = stockEventDto.toEvent();

    //then
    assertThat(event.getDocumentNumber(), is(stockEventDto.getDocumentNumber()));
    assertThat(event.getSignature(), is(stockEventDto.getSignature()));

    assertThat(event.getLineItems(), hasSize(stockEventDto.getLineItems().size()));

    assertThat(event.getProgramId(), is(stockEventDto.getProgramId()));
    assertThat(event.getFacilityId(), is(stockEventDto.getFacilityId()));

    assertThat(event.getUserId(), is(userId));

    ZonedDateTime processedDate = event.getProcessedDate();
    long between = SECONDS.between(processedDate, ZonedDateTime.now());

    assertThat(between, lessThan(2L));
  }

}