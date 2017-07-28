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

package org.openlmis.stockmanagement.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Immutable value object for a message that is localizable.
 */
public class Message {
  private String key;
  private Object[] params;

  public Message(String messageKey) {
    this(messageKey, (Object[]) null);
  }

  /**
   * Creates a new Message with parameters that optionally may be used when the message is
   * localized.
   *
   * @param messageKey        the key of the message
   * @param messageParameters the ordered parameters for substitution in a localized message.
   */
  public Message(String messageKey, Object... messageParameters) {
    Validate.notBlank(messageKey);
    this.key = messageKey.trim();
    this.params = messageParameters;
  }

  @Override
  public String toString() {
    return key + ": " + StringUtils.join(params, ", ");
  }

  /**
   * Gets the localized version of this message as it's intended for a human.
   *
   * @param messageSource the source of localized text.
   * @param locale        the locale to determine which localized text to use.
   * @return this message localized in a format suitable for serialization.
   * @throws org.springframework.context.NoSuchMessageException if the message doesn't exist in the
   *                                                            messageSource.
   */
  public LocalizedMessage localMessage(MessageSource messageSource, Locale locale) {
    return new LocalizedMessage(messageSource.getMessage(key, params, locale));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof Message)) {
      return false;
    }

    Message otherMessage = (Message) other;
    return this.key.equals(otherMessage.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  /**
   * Value class of a localized message.  Useful for JSON serialization, logging, etc...
   */
  public final class LocalizedMessage {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String messageKey;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String message;

    /**
     * Creates new LocalizedMessage based on given String.
     * @param message message.
     */
    public LocalizedMessage(String message) {
      Validate.notBlank(message);
      this.messageKey = Message.this.key;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return messageKey + ": " + message;
    }
  }
}
