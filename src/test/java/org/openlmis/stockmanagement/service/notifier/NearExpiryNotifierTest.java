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

package org.openlmis.stockmanagement.service.notifier;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

public class NearExpiryNotifierTest {

  private LotReferenceDataService lotReferenceDataService;

  private RightReferenceDataService rightReferenceDataService;

  private StockCardRepository stockCardRepository;

  private NearExpiryNotifier nearExpiryNotifier;
  
  private UUID expiringLotId;
  private UUID rightId;
  
  @Before
  public void setUp() {
    expiringLotId = UUID.randomUUID();
    lotReferenceDataService = mock(LotReferenceDataService.class);
    when(lotReferenceDataService.getAllLotsExpiringOn(any(LocalDate.class)))
        .thenReturn(Collections.singletonList(LotDto.builder().id(expiringLotId).build()));
    rightReferenceDataService = mock(RightReferenceDataService.class);
    rightId = UUID.randomUUID();
    RightDto mockRight = mock(RightDto.class);
    when(rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT))
        .thenReturn(mockRight);
    when(mockRight.getId()).thenReturn(rightId);
    StockCard testStockCard = StockCard.builder().build();
    testStockCard.setId(UUID.randomUUID());
    stockCardRepository = mock(StockCardRepository.class);
    when(stockCardRepository.findByLotIdIn(Collections.singleton(expiringLotId)))
        .thenReturn(Collections.singletonList(testStockCard));
    nearExpiryNotifier = spy(new NearExpiryNotifier(lotReferenceDataService,
        rightReferenceDataService, stockCardRepository));
    doNothing().when(nearExpiryNotifier).notifyEditors(any(StockCard.class), any(UUID.class));
    ReflectionTestUtils.setField(nearExpiryNotifier, "timeZoneId", "UTC");
  }
  
  @Test
  public void checkNearExpiryAndNotifyShouldNotifyIfExpiringStockFound() {
    nearExpiryNotifier.checkNearExpiryAndNotify();
    
    verify(nearExpiryNotifier).notifyEditors(any(StockCard.class), eq(rightId));
  }
  
  @Test
  public void checkNearExpiryAndNotifyShouldNotNotifyIfExpiringStockNotFound() {
    when(stockCardRepository.findByLotIdIn(Collections.singleton(expiringLotId)))
        .thenReturn(Collections.emptyList());

    nearExpiryNotifier.checkNearExpiryAndNotify();
    
    verify(nearExpiryNotifier, times(0))
        .notifyEditors(any(StockCard.class), eq(rightId));
  }
}
