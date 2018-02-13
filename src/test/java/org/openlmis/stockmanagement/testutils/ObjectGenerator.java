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

package org.openlmis.stockmanagement.testutils;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.util.NumberUtils.parseNumber;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.beanutils.PropertyUtils;
import org.assertj.core.util.Lists;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * The <strong>ObjectGenerator</strong> will create a random object for the given type.
 */
public final class ObjectGenerator {
  private static Multimap<Class<?>, Object> REFERENCES = HashMultimap.create();
  private static final Random RANDOM = new Random();

  private ObjectGenerator() {
    throw new UnsupportedOperationException();
  }

  public static <T> T of(Class<T> clazz) {
    return of(clazz, 1).get(0);
  }

  /**
   * Creates a list of instances with the given type. The list size will be equal to the number
   * in the <strong>count</strong> parameter.
   */
  public static <T> List<T> of(Class<T> clazz, int count) {
    while (REFERENCES.get(clazz).size() < count) {
      generate(clazz);
    }

    @SuppressWarnings("unchecked")
    // the map is update in private method where key and values has the same type
    // because values are generated from the passed class definition.
    Collection<T> collection = (Collection<T>) REFERENCES.get(clazz);
    return Lists.newArrayList(collection);
  }

  private static <T> void generate(Class<T> clazz) {
    Object instance;

    try {
      instance = clazz.newInstance();
    } catch (Exception exp) {
      throw new IllegalStateException("Missing no args constructor", exp);
    }

    for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(clazz)) {
      if ("class".equals(descriptor.getName())) {
        continue;
      }

      if (null == descriptor.getReadMethod() || null == descriptor.getWriteMethod()) {
        // we support only full property (it has to have a getter and setter)
        continue;
      }

      try {
        Object value = generateValue(clazz, descriptor.getPropertyType());
        PropertyUtils.setProperty(instance, descriptor.getName(), value);
      } catch (Exception exp) {
        throw new IllegalStateException(
            "Can't set value for property: " + descriptor.getName(), exp
        );
      }
    }

    REFERENCES.put(clazz, instance);
  }

  private static Object generateValue(Class<?> type, Class<?> propertyType) {
    Object value = generateBaseValue(propertyType);
    value = null == value ? generateCollectionValue(propertyType) : value;
    value = null == value ? generateCustomValue(propertyType) : value;

    if (null != value) {
      return value;
    }

    if (type.equals(propertyType)) {
      // if types are equals it means that the given DTO contains a element which is represent as
      // a child or parent. For now we return null.
      return null;
    }

    return of(propertyType);
  }

  private static Object generateCustomValue(Class<?> propertyType) {
    if (ObjectReferenceDto.class.isAssignableFrom(propertyType)) {
      return new ObjectReferenceDtoDataBuilder().build();
    }

    return null;
  }

  private static Object generateCollectionValue(Class<?> propertyType) {
    if (List.class.isAssignableFrom(propertyType)) {
      return emptyList();
    }

    if (Set.class.isAssignableFrom(propertyType)) {
      return emptySet();
    }

    if (Map.class.isAssignableFrom(propertyType)) {
      return emptyMap();
    }

    return null;
  }

  private static Object generateBaseValue(Class<?> propertyType) {
    if (String.class.isAssignableFrom(propertyType)) {
      return randomAlphanumeric(10);
    }

    if (Number.class.isAssignableFrom(propertyType)) {
      return parseNumber(String.valueOf(RANDOM.nextInt(1000) + 10), (Class<Number>) propertyType);
    }

    if (UUID.class.isAssignableFrom(propertyType)) {
      return randomUUID();
    }

    if (Boolean.class.isAssignableFrom(propertyType)
        || boolean.class.isAssignableFrom(propertyType)) {
      return true;
    }

    if (LocalDate.class.isAssignableFrom(propertyType)) {
      return LocalDate.now();
    }

    if (ZonedDateTime.class.isAssignableFrom(propertyType)) {
      return ZonedDateTime.now();
    }

    if (Enum.class.isAssignableFrom(propertyType)) {
      int idx = RANDOM.nextInt(propertyType.getEnumConstants().length);
      return propertyType.getEnumConstants()[idx];
    }

    return null;
  }

}
