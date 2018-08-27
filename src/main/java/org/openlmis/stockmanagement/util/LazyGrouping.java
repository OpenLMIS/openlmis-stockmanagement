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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LazyGrouping<K, V> implements Supplier<Map<K, V>> {
  private LazyList<V> collection;
  private Function<V, K> grouping;
  private Map<K, V> grouped;

  /**
   * Creates new instance of LazyGrouping.
   *
   * @param list     lazy list which will be used to retrieve data
   * @param grouping function which will be used to group data
   */
  public LazyGrouping(LazyList<V> list, Function<V, K> grouping) {
    this.collection = list;
    this.grouping = grouping;
    this.grouped = null;
  }

  /**
   * Retrieve grouped data. At the first time data will be retrieved from the lazy list and then
   * grouped. Next executions should not retrieved data.
   */
  public Map<K, V> get() {
    if (null != grouped) {
      return grouped;
    }

    grouped = collection
        .get()
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(grouping, element -> element));

    return grouped;
  }

  /**
   * Refresh current grouped data. Data will be loaded one more time from the lazy list and grouped
   * by grouping function.
   */
  void refresh() {
    grouped = null;
    collection.refresh();
  }
}