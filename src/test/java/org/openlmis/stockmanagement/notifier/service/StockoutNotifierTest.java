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

package org.openlmis.stockmanagement.notifier.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.EMAIL_ACTION_REQUIRED_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.EMAIL_ACTION_REQUIRED_SUBJECT;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.service.notification.NotificationService;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisoryNodeReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class StockoutNotifierTest {

  private static final String SUBJECT =
      "STOCKOUT Action Required: ${facilityName}, ${orderableName}";
  private static final String CONTENT = "Dear ${username}:\n"
      + "This email is informing you that there is 0 stock on hand for "
      + "${orderableNameLotInformation} in ${programName} at ${facilityName} as of ${stockoutDate}."
      + " As of today, this product has been stocked out for ${numberOfDaysOfStockout}.\n"
      + "Please login to view the bin card and take immediate action.\n"
      + "View bin card for ${orderableName}: ${urlToViewBinCard}\n"
      + "Initiate emergency requisition for this product: ${urlToInitiateRequisition}\n"
      + "Thank you.";
  private static final String TEST_KEY = "testKey";
  private static final String FACILITY_NAME = "Mock Facility";
  private static final String ORDERABLE_NAME = "Mock Orderable";
  private static final String URL_TO_VIEW_BIN_CARD =
      "/stockCardSummaries/{0}";
  private static final String URL_TO_INITIATE_REQUISITION =
      "/requisitions/initiate?facility={1}&program={0}&emergency={2}&supervised={3}";

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private MessageService messageService;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @Mock
  private SupervisingUsersReferenceDataService supervisingUsersReferenceDataService;

  @Mock
  private SupervisoryNodeReferenceDataService supervisoryNodeReferenceDataService;

  @Mock
  private LotReferenceDataService lotReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private StockoutNotifier stockoutNotifier;

  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private UUID rightId = UUID.randomUUID();
  private UUID supervisoryNodeId = UUID.randomUUID();

  private FacilityDto facility = mock(FacilityDto.class);
  private ProgramDto program = mock(ProgramDto.class);
  private OrderableDto orderable = mock(OrderableDto.class);
  private LotDto lot = mock(LotDto.class);
  private RightDto right = mock(RightDto.class);
  private SupervisoryNodeDto supervisoryNode = mock(SupervisoryNodeDto.class);
  private UserDto editor = mock(UserDto.class);
  private StockCard stockCard = mock(StockCard.class);
  private StockCardLineItem stockCardLineItem = mock(StockCardLineItem.class);

  private LocalDate stockoutDate = LocalDate.now().minusDays(5);

  @Before
  public void setUp() {
    when(facility.getName()).thenReturn(FACILITY_NAME);
    when(facility.getId()).thenReturn(facilityId);
    when(program.getName()).thenReturn("Mock Program");
    when(orderable.getFullProductName()).thenReturn(ORDERABLE_NAME);
    when(right.getId()).thenReturn(rightId);
    when(lot.getLotCode()).thenReturn("LOT 111");
    when(supervisoryNode.getId()).thenReturn(supervisoryNodeId);

    ReflectionTestUtils.setField(stockoutNotifier, "urlToViewBinCard",
        URL_TO_VIEW_BIN_CARD);
    ReflectionTestUtils.setField(stockoutNotifier, "urlToInitiateRequisition",
        URL_TO_INITIATE_REQUISITION);

    mockServices();
    mockStockCard();
  }

  @Test
  public void notifyStockEditorsShouldNotNotifyWhenFoundFacilityIsNotHomeFacility() {
    when(editor.getHomeFacilityId()).thenReturn(UUID.randomUUID());

    stockoutNotifier.notifyStockEditors(stockCard);

    verify(notificationService, times(0)).notify(any(), any(), any());
  }

  @Test
  public void notifyStockEditorsShouldNotNotifyWhenEditorHaveNoEmail() {
    when(editor.getEmail()).thenReturn(null);

    stockoutNotifier.notifyStockEditors(stockCard);

    verify(notificationService, times(0)).notify(any(), any(), any());
  }

  @Test
  public void notifyStockEditorsShouldNotNotifyWhenEditorIsNoActiveOrVerified() {
    when(editor.activeAndVerified()).thenReturn(false);

    stockoutNotifier.notifyStockEditors(stockCard);

    verify(notificationService, times(0)).notify(any(), any(), any());
  }

  @Test
  public void notifyStockEditorsShouldNotNotifyWhenEditorDoesNotAllowNotify() {
    when(editor.allowNotify()).thenReturn(false);

    stockoutNotifier.notifyStockEditors(stockCard);

    verify(notificationService, times(0)).notify(any(), any(), any());
  }

  @Test
  public void notifyStockEditorsShouldNotifyWithCorrectSubject() {
    stockoutNotifier.notifyStockEditors(stockCard);

    verify(notificationService).notify(
        eq(editor),
        eq("STOCKOUT Action Required: " + FACILITY_NAME + ", " + ORDERABLE_NAME),
        any());
  }

  @Test
  public void notifyStockEditorsShouldNotifyWithCorrectMessageBody() {
    testNotificationBody(stockoutDate, "5 days");
  }

  @Test
  public void notifyStockEditorsShouldNotifyWithCorrectMessageBodyForOneDayOfStockout() {
    LocalDate stockoutDate = LocalDate.now().minusDays(1);
    when(stockCardLineItem.getOccurredDate()).thenReturn(stockoutDate);

    testNotificationBody(stockoutDate, "1 day");
  }

  private void mockServices() {
    when(rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT)).thenReturn(right);
    when(supervisoryNodeReferenceDataService.findSupervisoryNode(programId, facilityId))
        .thenReturn(supervisoryNode);
    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, rightId, programId))
        .thenReturn(Collections.singleton(editor));
    mockUser(editor);
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facility);
    when(programReferenceDataService.findOne(programId)).thenReturn(program);
    when(orderableReferenceDataService.findOne(orderableId)).thenReturn(orderable);
    when(lotReferenceDataService.findOne(lotId)).thenReturn(lot);

    mockMessages();
  }

  private void testNotificationBody(LocalDate stockoutDate, String stockoutDays) {
    String urlToViewBinCard = MessageFormat.format(URL_TO_VIEW_BIN_CARD, stockCard.getId());
    String urlToInitiateRequisition = MessageFormat.format(URL_TO_INITIATE_REQUISITION,
        stockCard.getFacilityId(), stockCard.getProgramId(), "true", "false");
    String expected = "Dear editor:\n"
        + "This email is informing you that there is 0 stock on hand for "
        + "Mock Orderable LOT 111 in Mock Program at Mock Facility as "
        + "of " + getDateTimeFormatter().format(stockoutDate)
        + ". As of today, this product has been stocked out for " + stockoutDays + ".\n"
        + "Please login to view the bin card and take immediate action.\n"
        + "View bin card for Mock Orderable: " + urlToViewBinCard
        + "\nInitiate emergency requisition for this product: " + urlToInitiateRequisition
        + "\nThank you.";

    stockoutNotifier.notifyStockEditors(stockCard);

    verify(notificationService).notify(eq(editor), any(), eq(expected));
  }

  private void mockUser(UserDto editor) {
    when(editor.allowNotify()).thenReturn(true);
    when(editor.activeAndVerified()).thenReturn(true);
    when(editor.getEmail()).thenReturn("editor@mail.com");
    when(editor.getUsername()).thenReturn("editor");
    when(editor.getHomeFacilityId()).thenReturn(facilityId);
  }

  private void mockMessages() {
    Message.LocalizedMessage localizedMessage = new Message(TEST_KEY).new LocalizedMessage(SUBJECT);
    when(messageService.localize(new Message(EMAIL_ACTION_REQUIRED_SUBJECT)))
        .thenReturn(localizedMessage);
    localizedMessage = new Message(TEST_KEY).new LocalizedMessage(CONTENT);
    when(messageService.localize(new Message(EMAIL_ACTION_REQUIRED_CONTENT)))
        .thenReturn(localizedMessage);
  }

  private void mockStockCard() {
    when(stockCard.getId()).thenReturn(UUID.randomUUID());
    when(stockCard.getFacilityId()).thenReturn(facilityId);
    when(stockCard.getOrderableId()).thenReturn(orderableId);
    when(stockCard.getProgramId()).thenReturn(programId);
    when(stockCard.getLotId()).thenReturn(lotId);
    when(stockCard.getLineItems()).thenReturn(Collections.singletonList(stockCardLineItem));
    when(stockCardLineItem.getOccurredDate()).thenReturn(stockoutDate);
  }

  private DateTimeFormatter getDateTimeFormatter() {
    Locale locale = LocaleContextHolder.getLocale();

    String datePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.MEDIUM, null, Chronology.ofLocale(locale), locale);
    return DateTimeFormatter.ofPattern(datePattern);
  }
}