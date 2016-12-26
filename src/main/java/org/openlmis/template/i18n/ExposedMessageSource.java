package org.openlmis.template.i18n;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Map;

public interface ExposedMessageSource extends MessageSource {

  Map<String, String> getAllMessages(Locale locale);
}
