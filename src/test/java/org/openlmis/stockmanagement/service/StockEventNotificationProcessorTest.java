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

package org.openlmis.stockmanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventLineItem;

import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

@RunWith(MockitoJUnitRunner.class)
public class StockEventNotificationProcessorTest {

  @Mock
  private StockoutNotifier stockoutNotifier;
  
  @InjectMocks
  private StockEventNotificationProcessor stockEventNotificationProcessor;

  private UUID stockCardId = UUID.randomUUID();
  private UUID userId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  
  private StockCard stockCard;
  private StockEventProcessContext context;
  private StockEventDto stockEventDto;
  private StockEventLineItem firstLineItem;

  @Before
  public void setUp() {
    stockCard = new StockCard(null, facilityId, programId, orderableId, lotId, null, 0);
    stockCard.setId(stockCardId);

    context = mock(StockEventProcessContext.class);

    stockEventDto = createStockEventDto();
    stockEventDto.setUserId(userId);
    stockEventDto.setProgramId(programId);
    stockEventDto.setFacilityId(facilityId);
    firstLineItem = stockEventDto.getLineItems().get(0);
    firstLineItem.setOrderableId(orderableId);
    firstLineItem.setLotId(lotId);
    firstLineItem.setQuantity(0);

    stockEventDto.setContext(context);
  }
  
  @Test
  public void shouldCallStockoutNotifierWhenStockOnHandIsZero() throws Exception {
    //given
    when(context.findCard(any(OrderableLotIdentity.class))).thenReturn(stockCard);
    
    //when
    stockEventNotificationProcessor.callAllNotifications(stockEventDto);

    //then
    ArgumentCaptor<StockCard> captor = ArgumentCaptor.forClass(StockCard.class);
    verify(stockoutNotifier).notifyStockEditors(captor.capture());

    assertEquals(stockCardId, captor.getValue().getId());
    assertEquals(programId, captor.getValue().getProgramId());
    assertEquals(facilityId, captor.getValue().getFacilityId());
    assertEquals(orderableId, captor.getValue().getOrderableId());
    assertEquals(lotId, captor.getValue().getLotId());
  }

  @Test
  public void shouldCallStockoutNotifierForEveryCard() throws Exception {
    //given
    UUID anotherStockCardId = UUID.randomUUID();
    UUID anotherOrderableId = UUID.randomUUID();
    UUID anotherLotId = UUID.randomUUID();

    StockCard anotherStockCard = new StockCard(null, facilityId, programId, orderableId, lotId, 
        null, 0);
    anotherStockCard.setId(anotherStockCardId);

    StockEventLineItem secondLineItem = createStockEventLineItem();
    secondLineItem.setOrderableId(anotherOrderableId);
    secondLineItem.setLotId(anotherLotId);
    secondLineItem.setQuantity(0);
    stockEventDto.setLineItems(Arrays.asList(firstLineItem, secondLineItem));

    when(context.findCard(new OrderableLotIdentity(orderableId, lotId))).thenReturn(stockCard);
    when(context.findCard(new OrderableLotIdentity(anotherOrderableId, anotherLotId)))
        .thenReturn(anotherStockCard);

    //when
    stockEventNotificationProcessor.callAllNotifications(stockEventDto);

    //then
    verify(stockoutNotifier, times(2)).notifyStockEditors(any(StockCard.class));
  }
}
