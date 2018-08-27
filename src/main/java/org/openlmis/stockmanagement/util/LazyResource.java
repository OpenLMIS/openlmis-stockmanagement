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

import java.util.function.Supplier;

public class LazyResource<T> implements Supplier<T> {
  private T resource;
  private boolean retrieved;
  private Supplier<T> retriever;

  public LazyResource(Supplier<T> retriever) {
    this.retriever = retriever;
  }

  /**
   * Retrieve resource. At the first time resource will be retrieved from a source. Next executions
   * should not retrieved resource.
   */
  public T get() {
    if (retrieved) {
      return resource;
    }

    resource = retriever.get();
    retrieved = true;

    return resource;
  }

  /**
   * Refresh current resource. Data will be loaded one more time.
   */
  void refresh() {
    resource = null;
    retrieved = false;
  }

}