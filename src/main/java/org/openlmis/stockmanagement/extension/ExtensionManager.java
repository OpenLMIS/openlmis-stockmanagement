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

package org.openlmis.stockmanagement.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Class responsible for returning the right implementation of extension point.
 */
@Component
public class ExtensionManager {

  private static final String EXTENSIONS_DIR = "/extensions";
  private static final String CONFIG_FILE = "extensions.properties";
  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

  @Autowired
  private ApplicationContext appContext;

  @Getter
  @Setter
  private HashMap<String, String> extensions = new HashMap<String, String>();

  /**
   * Returns implementation of an extension with given id (based on config file) and class.
   * If mapping doesn't exist, returns one of given class (default).
   *
   * @param pointId id of extension point
   * @param pointClass class of extension point
   * @return extension point
   */
  public <T> T getExtension(String pointId, Class<T> pointClass) throws ExtensionException {
    T extensionPoint;
    String extensionId = extensions.get(pointId);
    try {
      if (extensionId != null) {
        extensionPoint = appContext.getBean(extensionId, pointClass);
      } else {
        extensionPoint = appContext.getBean(pointClass);
      }
    } catch (NoSuchBeanDefinitionException ex) {
      throw new ExtensionException("It was not possible to find an extension with id "
          + extensionId + " or " + pointClass, ex);
    }
    return extensionPoint;
  }

  public Object getExtensionByPointId(String pointId) throws ExtensionException {
    Object object;
    String extensionId = extensions.get(pointId);
    try {
      object = appContext.getBean(extensionId);
    } catch (NoSuchBeanDefinitionException ex) {
      throw new ExtensionException("It was not possible to find an extension with id "
          + extensionId, ex);
    }
    return object;
  }

  @PostConstruct
  private void loadConfigurationFile() {
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(new File(EXTENSIONS_DIR, CONFIG_FILE)));
      for (final Map.Entry<Object, Object> entry : prop.entrySet()) {
        extensions.put((String) entry.getKey(), (String) entry.getValue());
      }
    } catch (IOException ex) {
      LOGGER.debug("It was not possible to load extensions"
          + " configuration from extension.properties file", ex);
    }
  }
}