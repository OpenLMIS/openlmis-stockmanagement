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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_STOCKOUT_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_STOCKOUT_SUBJECT;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.FACILITY_NAME;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.LOT_CODE;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.ORDERABLE_NAME;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.PROGRAM_NAME;
import static org.openlmis.stockmanagement.service.notifier.BaseNotifierTest.URL_TO_VIEW_BIN_CARD;

import java.text.MessageFormat;
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
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.Message.LocalizedMessage;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class StockoutNotifierTest {

  private static final String NUM_DAYS_FIVE = "5 days";
  private static final String NUM_DAYS_ONE = "1 day";
  private static final String URL_TO_INITIATE_REQUISITION =
      "/requisitions/initiate?facility={1}&program={0}&emergency={2}&supervised={3}";

  @Mock
  private LotReferenceDataService lotReferenceDataService;

  @Mock
  private StockCardNotifier stockCardNotifier;

  @Mock
  private MessageService messageService;

  @InjectMocks
  private StockoutNotifier stockoutNotifier;

  private UUID stockCardId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private UUID rightId = UUID.randomUUID();

  private LotDto lot = mock(LotDto.class);
  private StockCard stockCard = mock(StockCard.class);
  private StockCardLineItem stockCardLineItem = mock(StockCardLineItem.class);

  private LocalDate stockoutDate = LocalDate.now().minusDays(5);

  @Before
  public void setUp() {
    when(stockCard.getId()).thenReturn(stockCardId);
    when(stockCard.getFacilityId()).thenReturn(facilityId);
    when(stockCard.getProgramId()).thenReturn(programId);
    when(stockCard.getOrderableId()).thenReturn(orderableId);
    when(stockCard.getLotId()).thenReturn(lotId);
    when(stockCard.getLineItems()).thenReturn(Collections.singletonList(stockCardLineItem));
    when(stockCardLineItem.getOccurredDate()).thenReturn(stockoutDate);

    when(stockCardNotifier.getFacilityName(facilityId)).thenReturn(FACILITY_NAME);
    when(stockCardNotifier.getProgramName(programId)).thenReturn(PROGRAM_NAME);
    when(stockCardNotifier.getOrderableName(orderableId)).thenReturn(ORDERABLE_NAME);
    when(lotReferenceDataService.findOne(lotId)).thenReturn(lot);
    when(stockCardNotifier.getDateFormatter()).thenReturn(DateTimeFormatter.ISO_LOCAL_DATE);
    when(stockCardNotifier.getUrlToViewBinCard(stockCardId)).thenReturn(URL_TO_VIEW_BIN_CARD);

    Message stockoutSubjectMessage = new Message(NOTIFICATION_STOCKOUT_SUBJECT);
    LocalizedMessage stockoutSubjectLocalizedMessage =
        stockoutSubjectMessage.new LocalizedMessage("stockout subject");
    when(messageService.localize(stockoutSubjectMessage))
        .thenReturn(stockoutSubjectLocalizedMessage);

    Message stockoutContentMessage = new Message(NOTIFICATION_STOCKOUT_CONTENT);
    LocalizedMessage stockoutContentLocalizedMessage =
        stockoutSubjectMessage.new LocalizedMessage("stockout content");
    when(messageService.localize(stockoutContentMessage))
        .thenReturn(stockoutContentLocalizedMessage);

    when(lot.getLotCode()).thenReturn(LOT_CODE);
    
    ReflectionTestUtils.setField(stockoutNotifier, "urlToInitiateRequisition",
        URL_TO_INITIATE_REQUISITION);
  }

  @Test
  public void notifyStockEditorsShouldNotify() {
    // when
    stockoutNotifier.notifyStockEditors(stockCard, rightId);

    // then
    verify(stockCardNotifier).notifyStockEditors(eq(stockCard), eq(rightId),
        any(NotificationMessageParams.class));
  }

  @Test
  public void constructSubstitutionMapShouldConstructSubstitutionMap() {
    // when
    Map<String, String> valuesMap = stockoutNotifier.constructSubstitutionMap(stockCard);

    // then
    assertEquals(FACILITY_NAME, valuesMap.get("facilityName"));
    assertEquals(PROGRAM_NAME, valuesMap.get("programName"));
    assertEquals(ORDERABLE_NAME, valuesMap.get("orderableName"));
    assertEquals(ORDERABLE_NAME + " " + LOT_CODE, valuesMap.get("orderableNameLotInformation"));
    String stockoutDateString = DateTimeFormatter.ISO_LOCAL_DATE.format(stockoutDate);
    assertEquals(stockoutDateString, valuesMap.get("stockoutDate"));
    assertEquals(NUM_DAYS_FIVE, valuesMap.get("numberOfDaysOfStockout"));
    assertEquals(URL_TO_VIEW_BIN_CARD, valuesMap.get("urlToViewBinCard"));
    String urlToInitiateRequisition = MessageFormat.format(URL_TO_INITIATE_REQUISITION,
        stockCard.getFacilityId(), stockCard.getProgramId(), "true", "false");
    assertEquals(urlToInitiateRequisition, valuesMap.get("urlToInitiateRequisition"));
  }

  @Test
  public void constructSubstitutionMapShouldReturnOrderableNameForLotInfoWhenNoLot() {
    // given
    when(stockCard.getLotId()).thenReturn(null);

    // when
    Map<String, String> valuesMap = stockoutNotifier.constructSubstitutionMap(stockCard);

    // then
    assertEquals(ORDERABLE_NAME, valuesMap.get("orderableNameLotInformation"));
  }

  @Test
  public void constructSubstitutionMapShouldReturnSingleDayStringForOneStockoutDay() {
    // given
    when(stockCardLineItem.getOccurredDate()).thenReturn(LocalDate.now().minusDays(1));

    // when
    Map<String, String> valuesMap = stockoutNotifier.constructSubstitutionMap(stockCard);

    // then
    assertEquals(NUM_DAYS_ONE, valuesMap.get("numberOfDaysOfStockout"));
  }
}
