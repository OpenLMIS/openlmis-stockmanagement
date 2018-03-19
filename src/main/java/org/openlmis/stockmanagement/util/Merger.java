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

package org.openlmis.stockmanagement.util;

import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.data.domain.Page;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Merger {

  private Merger() {
    throw new UnsupportedOperationException();
  }

  /**
   * Merge maps into single one that contain all key-value pairs from passed maps.
   */
  public static <K, V> Map<K, V> mergeMaps(List<Map<K, V>> maps) {
    return handle(maps, elements -> startStream(elements)
        .map(Map::entrySet)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  /**
   * Merge arrays into single one that contain all elements. If several arrays contain the same
   * element, it will be only one time in the result array.
   */
  public static <E> E[] mergeArrays(List<E[]> arrays) {
    return handle(arrays, elements -> startStream(elements)
        .flatMap(Arrays::stream)
        .distinct()
        .toArray(size -> {
          Class<?> componentType = arrays.get(0).getClass().getComponentType();
          return (E[]) Array.newInstance(componentType, size);
        }));
  }

  /**
   * Merge pages into single one that contain all elements. If several pages contain the same
   * element, it will be only one time in the result page.
   */
  public static <E> PageImplRepresentation<E> mergePages(List<PageImplRepresentation<E>> pages) {
    return handle(pages, elements -> {
      List<E> content = startStream(elements)
          .map(PageImplRepresentation::getContent)
          .flatMap(Collection::stream)
          .distinct()
          .collect(Collectors.toList());

      Page<E> page = Pagination.getPage(content);
      return new PageImplRepresentation<>(page);
    });
  }

  private static <E> E handle(List<E> list, Function<List<E>, E> mergeFunction) {
    if (null == list || list.isEmpty()) {
      return null;
    }

    if (list.size() == 1) {
      return list.get(0);
    }

    return mergeFunction.apply(list);
  }

  private static <E> Stream<E> startStream(List<E> list) {
    return list.stream().filter(Objects::nonNull);
  }

}
