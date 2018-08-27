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

package org.openlmis.stockmanagement.errorhandling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TAGS_INVALID;

import java.sql.SQLException;
import javax.persistence.PersistenceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.orm.jpa.JpaSystemException;

@RunWith(MockitoJUnitRunner.class)
public class GlobalErrorHandlingTest {

  private static final String MESSAGE = "message";
  private static final String TRANSLATED_MESSAGE = "translated message";

  @Mock
  private MessageService messageService;

  @InjectMocks
  private GlobalErrorHandling globalErrorHandling;


  private Message.LocalizedMessage localizedMessage;

  @Before
  public void setup() {
    localizedMessage = new Message(MESSAGE).new LocalizedMessage(TRANSLATED_MESSAGE);
    when(messageService.localize(new Message(MESSAGE))).thenReturn(localizedMessage);
  }

  @Test
  public void shouldHandleSqlException() {
    Message.LocalizedMessage translatedMessage = new Message(ERROR_LINE_ITEM_REASON_TAGS_INVALID)
        .new LocalizedMessage("sql message");
    when(messageService.localize(new Message(ERROR_LINE_ITEM_REASON_TAGS_INVALID)))
        .thenReturn(translatedMessage);

    JpaSystemException ex = new JpaSystemException(
        new PersistenceException(
            new SQLException("sql error", "22001", new NullPointerException(MESSAGE))));

    Message.LocalizedMessage message = globalErrorHandling.handleJpaSystemException(ex);
    assertEquals(translatedMessage, message);
  }

  @Test
  public void shouldReturnNormalMessageIfExceptionIsNotPersistenceException() {
    JpaSystemException ex = new JpaSystemException(new NullPointerException(MESSAGE));

    Message.LocalizedMessage message = globalErrorHandling.handleJpaSystemException(ex);
    assertEquals(globalErrorHandling.getLocalizedMessage(TRANSLATED_MESSAGE), message);
  }

  @Test
  public void shouldReturnNormalMessageIfExceptionIsNotSqlException() {
    JpaSystemException ex = new JpaSystemException(
        new PersistenceException(new NullPointerException(MESSAGE)));

    Message.LocalizedMessage message = globalErrorHandling.handleJpaSystemException(ex);
    assertEquals(globalErrorHandling.getLocalizedMessage(TRANSLATED_MESSAGE), message);
  }

  @Test
  public void shouldReturnNormalMessageIfSqlStatusIsNotRecognizable() {
    JpaSystemException ex = new JpaSystemException(
        new PersistenceException(
            new SQLException("sql error", "10", new NullPointerException(MESSAGE))));

    Message.LocalizedMessage message = globalErrorHandling.handleJpaSystemException(ex);
    assertEquals(globalErrorHandling.getLocalizedMessage(TRANSLATED_MESSAGE), message);
  }
}
