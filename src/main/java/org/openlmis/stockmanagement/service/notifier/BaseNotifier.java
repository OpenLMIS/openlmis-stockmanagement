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

import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

public class BaseNotifier {

  @Autowired
  protected MessageService messageService;

  /**
   * Check if user want notifications: (enabled, verified, allowNotify all true).
   */
  protected static boolean canBeNotified(UserDto user) {
    return user != null && user.allowNotify()
        && user.activeAndVerified() && user.getEmail() != null;
  }

  protected String getMessage(String key) {
    return messageService
        .localize(new Message(key))
        .getMessage();
  }

  protected DateTimeFormatter getDateTimeFormatter() {
    Locale locale = LocaleContextHolder.getLocale();

    String datePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.MEDIUM, null, Chronology.ofLocale(locale), locale);
    return DateTimeFormatter.ofPattern(datePattern);
  }
}
