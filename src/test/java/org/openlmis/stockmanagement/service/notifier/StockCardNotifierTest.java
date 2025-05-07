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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.service.notification.NotificationService;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisoryNodeReferenceDataService;
import org.openlmis.stockmanagement.util.Message;

@RunWith(MockitoJUnitRunner.class)
public class StockCardNotifierTest {

  private static final String DUMMY_KEY = "dummyKey";
  private static final String SUBJECT_KEY = "subject";
  private static final String CONTENT_KEY = "content";
  private static final String MESSAGE_SUBJECT = "${" + SUBJECT_KEY + "}";
  private static final String MESSAGE_CONTENT = "${" + CONTENT_KEY + "}";
  private static final String SUBJECT_VALUE = "This is the subject";
  private static final String CONTENT_VALUE = "This is the content";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private MessageService messageService;
  
  @Mock
  private SupervisingUsersReferenceDataService supervisingUsersReferenceDataService;

  @Mock
  private SupervisoryNodeReferenceDataService supervisoryNodeReferenceDataService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private StockCardNotifier stockCardNotifier;

  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID rightId = UUID.randomUUID();
  private UUID supervisoryNodeId = UUID.randomUUID();

  private UserDto editor = mock(UserDto.class);
  private StockCard stockCard = mock(StockCard.class);
  private SupervisoryNodeDto supervisoryNode = new SupervisoryNodeDto(); //cannot mock final class
  private NotificationMessageParams params = mock(NotificationMessageParams.class);
  
  @Before
  public void setUp() {
    when(stockCard.getFacilityId()).thenReturn(facilityId);
    when(stockCard.getProgramId()).thenReturn(programId);
    supervisoryNode.setId(supervisoryNodeId);
    when(supervisoryNodeReferenceDataService.findSupervisoryNode(programId, facilityId))
        .thenReturn(supervisoryNode);
    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, rightId, programId))
        .thenReturn(Collections.singletonList(editor));
    when(params.getMessageSubject()).thenReturn(MESSAGE_SUBJECT);
    when(params.getMessageContent()).thenReturn(MESSAGE_CONTENT);
    Map<String, String> substitutionMap = new HashMap<>();
    substitutionMap.put(SUBJECT_KEY, SUBJECT_VALUE);
    substitutionMap.put(CONTENT_KEY, CONTENT_VALUE);
    when(params.getSubstitutionMap()).thenReturn(substitutionMap);
    when(editor.getHomeFacilityId()).thenReturn(facilityId);
    
    mockMessages(); // for BaseNotifier.getMessage()
  }

  @Test
  public void shouldNotThrowErrorDuringNotificationWhenSomeUsersHasNoHomeFacility() {
    UserDto editorWithoutHomeFacility = mock(UserDto.class);
    when(editorWithoutHomeFacility.getHomeFacilityId()).thenReturn(null);

    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, rightId, programId))
        .thenReturn(Collections.singletonList(editorWithoutHomeFacility));

    stockCardNotifier.notifyStockEditors(stockCard, rightId, params);
  }

  @Test
  public void notifyStockEditorsShouldNotifyStockEditors() {
    // when
    stockCardNotifier.notifyStockEditors(stockCard, rightId, params);
    
    // then
    verify(notificationService).notify(editor, SUBJECT_VALUE, CONTENT_VALUE);
  }
  
  @Test
  public void notifyStockEditorsShouldNotNotifyIfFacilityIsNotHomeFacility() {
    // given
    when(editor.getHomeFacilityId()).thenReturn(UUID.randomUUID());

    // when
    stockCardNotifier.notifyStockEditors(stockCard, rightId, params);

    // then
    verify(notificationService, times(0)).notify(any(UserDto.class),
        any(String.class), any(String.class));
  }
  
  @Test
  public void notifyStockEditorsShouldNotNotifyIfNoUsersForNode() {
    // given
    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, rightId, programId))
        .thenReturn(Collections.emptyList());

    // when
    stockCardNotifier.notifyStockEditors(stockCard, rightId, params);

    // then
    verify(notificationService, times(0)).notify(any(UserDto.class),
        any(String.class), any(String.class));
  }
  
  @Test
  public void notifyStockEditorsShouldNotNotifyIfNoSupervisoryNode() {
    // given
    when(supervisoryNodeReferenceDataService.findSupervisoryNode(programId, facilityId))
        .thenReturn(null);
    exception.expect(IllegalArgumentException.class);

    // when
    stockCardNotifier.notifyStockEditors(stockCard, rightId, params);
    
    // then exception
  }

  private void mockMessages() {
    Message.LocalizedMessage localizedMessage = new Message(DUMMY_KEY)
        .new LocalizedMessage(MESSAGE_SUBJECT);
    lenient().when(messageService.localize(new Message(MESSAGE_SUBJECT)))
        .thenReturn(localizedMessage);

    localizedMessage = new Message(DUMMY_KEY).new LocalizedMessage(MESSAGE_CONTENT);
    lenient().when(messageService.localize(new Message(MESSAGE_CONTENT)))
        .thenReturn(localizedMessage);
  }
}
