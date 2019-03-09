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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.FACILITY_NAME;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.LOT_CODE;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.ORDERABLE_NAME;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.PROGRAM_NAME;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.URL_TO_VIEW_BIN_CARD;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class NearExpiryNotifierTest {

  private static final String TEST_DATE = "2019-01-01";
  
  @Mock
  private LotReferenceDataService lotReferenceDataService;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @Mock
  private StockCardRepository stockCardRepository;

  @Mock
  private StockCardNotifier stockCardNotifier;

  @Mock
  private RightDto mockRight;

  @Mock
  private StockCard testStockCard;

  @Mock
  private LotDto expiringLot;

  @InjectMocks
  private NearExpiryNotifier nearExpiryNotifier;
  
  private UUID expiringLotId = UUID.randomUUID();
  private UUID rightId = UUID.randomUUID();
  
  @Before
  public void setUp() {
    when(lotReferenceDataService.getAllLotsExpiringOn(any(LocalDate.class)))
        .thenReturn(Collections.singletonList(expiringLot));
    when(rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT))
        .thenReturn(mockRight);
    when(mockRight.getId()).thenReturn(rightId);
    when(stockCardRepository.findByLotIdIn(Collections.singleton(expiringLotId)))
        .thenReturn(Collections.singletonList(testStockCard));
    when(testStockCard.getLotId()).thenReturn(expiringLotId);

    when(stockCardNotifier.getFacilityName(any(UUID.class))).thenReturn(FACILITY_NAME);
    when(stockCardNotifier.getProgramName(any(UUID.class))).thenReturn(PROGRAM_NAME);
    when(stockCardNotifier.getOrderableName(any(UUID.class))).thenReturn(ORDERABLE_NAME);
    when(stockCardNotifier.getDateFormatter()).thenReturn(DateTimeFormatter.ISO_LOCAL_DATE);
    when(stockCardNotifier.getUrlToViewBinCard(any(UUID.class))).thenReturn(URL_TO_VIEW_BIN_CARD);

    ReflectionTestUtils.setField(nearExpiryNotifier, "timeZoneId", "UTC");
    ReflectionTestUtils.setField(nearExpiryNotifier, "expiringLotMap",
        Collections.singletonMap(expiringLotId, expiringLot));
    when(expiringLot.getId()).thenReturn(expiringLotId);
    when(expiringLot.getLotCode()).thenReturn(LOT_CODE);
    ReflectionTestUtils.setField(nearExpiryNotifier, "expirationDate", LocalDate.parse(TEST_DATE));
  }
  
  @Test
  public void checkNearExpiryAndNotifyShouldNotifyIfExpiringStockFound() {
    // when
    nearExpiryNotifier.checkNearExpiryAndNotify();
    
    // then
    verify(stockCardNotifier).notifyStockEditors(any(StockCard.class), eq(rightId),
        any(NotificationMessageParams.class));
  }
  
  @Test
  public void checkNearExpiryAndNotifyShouldNotNotifyIfExpiringStockNotFound() {
    // given
    when(stockCardRepository.findByLotIdIn(Collections.singleton(expiringLotId)))
        .thenReturn(Collections.emptyList());

    // when
    nearExpiryNotifier.checkNearExpiryAndNotify();
    
    // then
    verify(stockCardNotifier, times(0)).notifyStockEditors(
        any(StockCard.class), eq(rightId), any(NotificationMessageParams.class));
  }
  
  @Test
  public void constructSubstitutionMapShouldConstructSubstitutionMap() {
    // when
    Map<String, String> valuesMap = nearExpiryNotifier.constructSubstitutionMap(testStockCard);

    // then
    assertEquals(FACILITY_NAME, valuesMap.get("facilityName"));
    assertEquals(PROGRAM_NAME, valuesMap.get("programName"));
    assertEquals(ORDERABLE_NAME, valuesMap.get("orderableName"));
    assertEquals(LOT_CODE, valuesMap.get("lotCode"));
    assertEquals(TEST_DATE, valuesMap.get("expirationDate"));
    assertEquals(URL_TO_VIEW_BIN_CARD, valuesMap.get("urlToViewBinCard"));
  }
  
  @Test
  public void constructSubstitutionMapShouldReturnEmptyLotCodeForNoLot() {
    // given
    ReflectionTestUtils.setField(nearExpiryNotifier, "expiringLotMap",
        Collections.singletonMap(expiringLotId, null));
    
    // when
    Map<String, String> valuesMap = nearExpiryNotifier.constructSubstitutionMap(testStockCard);

    // then
    assertEquals("", valuesMap.get("lotCode"));
  }
}
