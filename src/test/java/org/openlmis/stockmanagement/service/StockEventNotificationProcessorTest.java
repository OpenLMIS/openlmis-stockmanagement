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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDate;
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDateTime;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventLineItem;

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
import org.openlmis.stockmanagement.domain.identity.OrderableLotUnitIdentity;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

@RunWith(MockitoJUnitRunner.class)
public class StockEventNotificationProcessorTest {

  @Mock
  private StockoutNotifier stockoutNotifier;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @InjectMocks
  private StockEventNotificationProcessor stockEventNotificationProcessor;

  private UUID stockCardId = UUID.randomUUID();
  private UUID userId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private UUID unitOfOrderableId = UUID.randomUUID();
  private UUID rightId = UUID.randomUUID();

  private RightDto right = mock(RightDto.class);
  private StockCard stockCard;
  private StockEventProcessContext context;
  private StockEventDto stockEventDto;
  private StockEventLineItemDto firstLineItem;

  @Before
  public void setUp() {
    stockCard = new StockCard(null, facilityId, programId, orderableId, lotId,
        unitOfOrderableId, null, 0,
        getBaseDate(), getBaseDateTime(), true);
    stockCard.setId(stockCardId);

    context = mock(StockEventProcessContext.class);

    stockEventDto = createStockEventDto();
    stockEventDto.setUserId(userId);
    stockEventDto.setActive(true);
    stockEventDto.setProgramId(programId);
    stockEventDto.setFacilityId(facilityId);
    firstLineItem = stockEventDto.getLineItems().get(0);
    firstLineItem.setOrderableId(orderableId);
    firstLineItem.setLotId(lotId);
    firstLineItem.setUnitOfOrderableId(unitOfOrderableId);
    firstLineItem.setQuantity(0);

    stockEventDto.setContext(context);

    when(right.getId()).thenReturn(rightId);
    when(rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT)).thenReturn(right);
  }

  @Test
  public void shouldCallStockoutNotifierWhenStockOnHandIsZero() throws Exception {
    //given
    when(context.findCard(any(OrderableLotUnitIdentity.class))).thenReturn(stockCard);

    //when
    stockEventNotificationProcessor.callAllNotifications(stockEventDto);

    //then
    ArgumentCaptor<StockCard> captor = ArgumentCaptor.forClass(StockCard.class);
    verify(stockoutNotifier).notifyStockEditors(captor.capture(), eq(rightId));

    assertEquals(stockCardId, captor.getValue().getId());
    assertEquals(programId, captor.getValue().getProgramId());
    assertEquals(facilityId, captor.getValue().getFacilityId());
    assertEquals(orderableId, captor.getValue().getOrderableId());
    assertEquals(lotId, captor.getValue().getLotId());
  }

  @Test
  public void shouldCallStockoutNotifierForEveryCard() throws Exception {
    //given
    final UUID anotherStockCardId = UUID.randomUUID();
    final UUID anotherOrderableId = UUID.randomUUID();
    final UUID anotherLotId = UUID.randomUUID();
    final UUID anotherUnitOfOrderableId = UUID.randomUUID();

    StockCard anotherStockCard = new StockCard(null, facilityId, programId, orderableId,
        lotId, unitOfOrderableId,
        null, 0, getBaseDate(), getBaseDateTime(), true);
    anotherStockCard.setId(anotherStockCardId);

    StockEventLineItemDto secondLineItem = createStockEventLineItem();
    secondLineItem.setOrderableId(anotherOrderableId);
    secondLineItem.setLotId(anotherLotId);
    secondLineItem.setUnitOfOrderableId(anotherUnitOfOrderableId);
    secondLineItem.setQuantity(0);
    stockEventDto.setLineItems(Arrays.asList(firstLineItem, secondLineItem));

    when(context.findCard(new OrderableLotUnitIdentity(orderableId, lotId, unitOfOrderableId)))
        .thenReturn(stockCard);
    when(context.findCard(
        new OrderableLotUnitIdentity(anotherOrderableId, anotherLotId, anotherUnitOfOrderableId)
    ))
        .thenReturn(anotherStockCard);

    //when
    stockEventNotificationProcessor.callAllNotifications(stockEventDto);

    //then
    verify(stockoutNotifier, times(2)).notifyStockEditors(
        any(StockCard.class), eq((rightId))
    );
  }
}
