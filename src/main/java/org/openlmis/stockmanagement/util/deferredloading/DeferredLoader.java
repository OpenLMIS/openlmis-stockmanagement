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

package org.openlmis.stockmanagement.util.deferredloading;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A Deferred Loading is meant to reduce the number of REST requests between services by
 * replacing many "read one" requests with a single "read many". See implementations for examples.
 *
 * @param <D> the type of DTO
 * @param <K> the type of key, usually UUID
 */
public abstract class DeferredLoader<D, K> {
  protected final Map<K, DeferredObject<D, K>> deferredObjects;

  DeferredLoader() {
    this(new HashMap<>());
  }

  DeferredLoader(Map<K, DeferredObject<D, K>> deferredObjects) {
    this.deferredObjects = requireNonNull(deferredObjects);
  }

  /**
   * Gets a handle for object to be loaded later. The method returns the same instance of the
   * handler for the same {@code key}.
   *
   * @param key the key, not null
   * @return a handle for object to be loaded later, never null
   */
  public DeferredObject<D, K> deferredLoad(K key) {
    if (key == null) {
      return null;
    }

    return deferredObjects.computeIfAbsent(key, k -> new DeferredObject<>(key));
  }

  public abstract void loadDeferredObjects();
}
