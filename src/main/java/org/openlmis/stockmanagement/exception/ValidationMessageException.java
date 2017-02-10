package org.openlmis.stockmanagement.exception;

import org.openlmis.stockmanagement.utils.Message;

public class ValidationMessageException extends BaseMessageException {

  public ValidationMessageException(Message message) {
    super(message);
  }

  public ValidationMessageException(String messageKey) {
    super(messageKey);
  }
}
