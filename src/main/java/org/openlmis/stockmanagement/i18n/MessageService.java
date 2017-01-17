package org.openlmis.stockmanagement.i18n;

import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

  @Autowired
  private ExposedMessageSource messageSource;

  public Message.LocalizedMessage localize(Message message) {
    return message.localMessage(messageSource, LocaleContextHolder.getLocale());
  }

}
