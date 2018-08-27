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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.Test;

public class LazyResourceTest {
  private AtomicInteger count = new AtomicInteger(0);

  private Object resource = new Object();

  private Supplier<Object> retriever = () -> {
    count.incrementAndGet();
    return resource;
  };

  private LazyResource<Object> lazy = new LazyResource<>(retriever);

  @Test
  public void shouldRetrieveResourceFromSource() throws Exception {
    Object retrieved = lazy.get();
    assertThat(retrieved, equalTo(resource));
    assertThat(count.get(), equalTo(1));
  }

  @Test
  public void shouldNotRetrieveResourceAfterFirstExecution() throws Exception {
    Object retrieved = lazy.get();
    assertThat(retrieved, equalTo(resource));

    lazy.get();
    lazy.get();
    lazy.get();
    lazy.get();

    assertThat(count.get(), equalTo(1));
  }

  @Test
  public void shouldRetrieveResourceAfterRefresh() throws Exception {
    Object retrieved = lazy.get();
    assertThat(retrieved, equalTo(resource));

    lazy.refresh();
    retrieved = lazy.get();
    assertThat(retrieved, equalTo(resource));

    assertThat(count.get(), equalTo(2));
  }
}