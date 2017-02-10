package org.openlmis.stockmanagement.exception;

import org.openlmis.stockmanagement.utils.Message;

/**
 * Base class for exceptions using Message.
 */
public class BaseMessageException extends RuntimeException {
  private final Message message;

  public BaseMessageException(Message message) {
    this.message = message;
  }

  public BaseMessageException(String messageKey) {
    this.message = new Message(messageKey);
  }

  public Message asMessage() {
    return message;
  }

  /**
   * Overrides RuntimeException's public String getMessage().
   *
   * @return a localized string description
   */
  @Override
  public String getMessage() {
    return this.message.toString();
  }
}
