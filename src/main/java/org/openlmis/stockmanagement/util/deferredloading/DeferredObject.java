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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Allows to create objects that creation is deferred for later.
 * Example, a reference to Dto from another service, that will be loaded as part of single read
 * request.
 *
 * @param <D> the type of deferred object
 * @param <K> the unique object key
 */
public class DeferredObject<D, K> implements Supplier<D> {
  private boolean initiated;
  private D value;
  private final K objectKey;

  DeferredObject(K objectKey) {
    this.initiated = false;
    this.objectKey = requireNonNull(objectKey);
  }

  @Override
  public D get() {
    if (!initiated) {
      throw new IllegalStateException("Un-initiated deferred object used, key: " + objectKey);
    }

    return value;
  }

  void set(D value) {
    if (initiated) {
      throw new IllegalStateException("Initiated deferred object set, " + objectKey);
    }

    this.initiated = true;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeferredObject<?, ?> that = (DeferredObject<?, ?>) o;
    return Objects.equals(objectKey, that.objectKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectKey);
  }
}
