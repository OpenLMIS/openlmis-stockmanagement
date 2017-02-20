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

package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.i18n.ExposedMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MessageController {

  Logger logger = LoggerFactory.getLogger(MessageController.class);

  @Autowired
  private ExposedMessageSource messageSource;

  /**
   * Returns "hello world" message.
   *
   * @return value of template.message.hello
   */
  @RequestMapping("/hello")
  public String hello() {
    String[] msgArgs = {"world"};
    logger.debug("Returning hello world message");
    return messageSource.getMessage("template.message.hello", msgArgs,
            LocaleContextHolder.getLocale());
  }

  @RequestMapping("/messages")
  public Map<String, String> getAllMessages() {
    logger.info("Returning all messages for current locale");
    return messageSource.getAllMessages(LocaleContextHolder.getLocale());
  }
}
