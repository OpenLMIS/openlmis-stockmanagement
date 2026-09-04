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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Map;
import org.junit.Test;

public class DeferredLoaderTest {
  @Test(expected = NullPointerException.class)
  public void shouldThrowWhenNullInConstructor() {
    new TestDeferredLoader(null);
  }

  @Test
  public void shouldReturnNullIfNullKey() {
    final DeferredLoader<String, String, DeferredObject<String, String>> loader =
        new TestDeferredLoader();
    assertNull(loader.deferredLoad(null));
  }

  @Test
  public void shouldReturnTheSameHandlerForTheSameKey() {
    final DeferredLoader<String, String, DeferredObject<String, String>> loader =
        new TestDeferredLoader();
    final String sameKey = "abc321";

    assertSame(loader.deferredLoad(sameKey), loader.deferredLoad(sameKey));
  }

  private static final class TestDeferredLoader
      extends DeferredLoader<String, String, DeferredObject<String, String>> {

    TestDeferredLoader() {
      super();
    }

    TestDeferredLoader(Map<String, DeferredObject<String, String>> deferredObjects) {
      super(deferredObjects);
    }

    @Override
    protected DeferredObject<String, String> newHandle(String key) {
      return new DeferredObject<String, String>(key) {
      };
    }

    @Override
    public void loadDeferredObjects() {
      // do nothing
    }
  }
}
