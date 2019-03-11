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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_NEAR_EXPIRY_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_NEAR_EXPIRY_SUBJECT;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;
import static org.openlmis.stockmanagement.service.notifier.StockoutNotifierTest.FACILITY_NAME;
import static org.openlmis.stockmanagement.service.notifier.StockoutNotifierTest.LOT_CODE;
import static org.openlmis.stockmanagement.service.notifier.StockoutNotifierTest.ORDERABLE_NAME;
import static org.openlmis.stockmanagement.service.notifier.StockoutNotifierTest.PROGRAM_NAME;
import static org.openlmis.stockmanagement.service.notifier.StockoutNotifierTest.URL_TO_VIEW_BIN_CARD;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
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

  private static final String TEST_DATE = "2019-01-01";
  
  private LotReferenceDataService lotReferenceDataService;

  private RightReferenceDataService rightReferenceDataService;

  private StockCardRepository stockCardRepository;

  private NearExpiryNotifier nearExpiryNotifier;
  
  private UUID expiringLotId;
  private LotDto expiringLot;
  private UUID rightId;
  private UUID facilityId;
  private UUID programId;
  private UUID orderableId;
  private StockCard testStockCard;
  
  @Before
  public void setUp() {
    expiringLotId = UUID.randomUUID();
    expiringLot = LotDto.builder().id(expiringLotId).lotCode(LOT_CODE).build();
    lotReferenceDataService = mock(LotReferenceDataService.class);
    when(lotReferenceDataService.getAllLotsExpiringOn(any(LocalDate.class)))
        .thenReturn(Collections.singletonList(expiringLot));
    rightReferenceDataService = mock(RightReferenceDataService.class);
    rightId = UUID.randomUUID();
    RightDto mockRight = mock(RightDto.class);
    when(rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT))
        .thenReturn(mockRight);
    when(mockRight.getId()).thenReturn(rightId);
    testStockCard = StockCard.builder()
        .facilityId(facilityId)
        .programId(programId)
        .orderableId(orderableId)
        .lotId(expiringLotId)
        .build();
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
  
  @Test
  public void getValuesMapShouldPopulateValuesMap() {
    prepareForGetValuesMap();

    Map<String, String> valuesMap = nearExpiryNotifier.getValuesMap(testStockCard);
    
    assertEquals(FACILITY_NAME, valuesMap.get("facilityName"));
    assertEquals(PROGRAM_NAME, valuesMap.get("programName"));
    assertEquals(ORDERABLE_NAME, valuesMap.get("orderableName"));
    assertEquals(LOT_CODE, valuesMap.get("lotCode"));
    assertEquals(TEST_DATE, valuesMap.get("expirationDate"));
    assertEquals(URL_TO_VIEW_BIN_CARD, valuesMap.get("urlToViewBinCard"));
  }
  
  @Test
  public void getValuesMapShouldReturnEmptyLotCodeForNoLot() {
    prepareForGetValuesMap();
    ReflectionTestUtils.setField(nearExpiryNotifier, "expiringLotMap",
        Collections.singletonMap(expiringLotId, null));
    
    Map<String, String> valuesMap = nearExpiryNotifier.getValuesMap(testStockCard);

    assertEquals("", valuesMap.get("lotCode"));
  }
  
  private void prepareForGetValuesMap() {
    doReturn(FACILITY_NAME).when(nearExpiryNotifier).getFacilityName(any(UUID.class));
    doReturn(PROGRAM_NAME).when(nearExpiryNotifier).getProgramName(any(UUID.class));
    doReturn(ORDERABLE_NAME).when(nearExpiryNotifier).getOrderableName(any(UUID.class));
    doReturn(DateTimeFormatter.ISO_LOCAL_DATE).when(nearExpiryNotifier).getDateFormatter();
    doReturn(URL_TO_VIEW_BIN_CARD).when(nearExpiryNotifier)
        .getUrlToViewBinCard(any(StockCard.class));
    ReflectionTestUtils.setField(nearExpiryNotifier, "expiringLotMap",
        Collections.singletonMap(expiringLotId, expiringLot));
    ReflectionTestUtils.setField(nearExpiryNotifier, "expirationDate", LocalDate.parse(TEST_DATE));
  }
  
  @Test
  public void getMessageSubjectShouldGetSubject() {
    assertEquals(NOTIFICATION_NEAR_EXPIRY_SUBJECT, nearExpiryNotifier.getMessageSubject());
  }
  
  @Test
  public void getMessageContentShouldGetContent() {
    assertEquals(NOTIFICATION_NEAR_EXPIRY_CONTENT, nearExpiryNotifier.getMessageContent());
  }
}
