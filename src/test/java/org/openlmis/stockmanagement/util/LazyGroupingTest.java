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

import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class LazyGroupingTest {
  private AtomicInteger count = new AtomicInteger(0);

  private Object resource = new Object();
  private List<Object> resources = Lists.newArrayList(resource);

  private Supplier<List<Object>> retriever = () -> {
    count.incrementAndGet();
    return resources;
  };

  private LazyList<Object> lazy = new LazyList<>(retriever);
  private LazyGrouping<Integer, Object> group = new LazyGrouping<>(lazy, a -> 1);

  @Test
  public void shouldRetrieveResourceFromSource() throws Exception {
    Object retrieved = group.get().get(1);
    assertThat(retrieved, equalTo(resource));
    assertThat(count.get(), equalTo(1));
  }

  @Test
  public void shouldNotRetrieveResourceAfterFirstExecution() throws Exception {
    Object retrieved = group.get().get(1);
    assertThat(retrieved, equalTo(resource));

    group.get();
    group.get();
    group.get();
    group.get();

    assertThat(count.get(), equalTo(1));
  }

  @Test
  public void shouldRetrieveResourceAfterRefresh() throws Exception {
    Object retrieved = group.get().get(1);
    assertThat(retrieved, equalTo(resource));

    group.refresh();
    retrieved = group.get().get(1);
    assertThat(retrieved, equalTo(resource));

    assertThat(count.get(), equalTo(2));
  }
}