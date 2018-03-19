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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

public class MergerTest {

  @Test
  public void shouldReturnNullIfArgumentIsNull() {
    assertNull(Merger.mergeArrays(null));
    assertNull(Merger.mergeMaps(null));
    assertNull(Merger.mergePages(null));
  }

  @Test
  public void shouldReturnNullIfArgumentListIsEmpty() {
    assertNull(Merger.mergeArrays(newArrayList()));
    assertNull(Merger.mergeMaps(newArrayList()));
    assertNull(Merger.mergePages(newArrayList()));
  }

  @Test
  public void shouldReturnElementIfArgumentListContainsOnlyOne() {
    List<Map<String, String>> maps = ImmutableList.of(ImmutableMap.of("a", "b"));
    List<String[]> arrays = ImmutableList.of(new String[]{"a", "b"});
    List<PageImplRepresentation<String>> pages = ImmutableList
        .of(new PageImplRepresentation<>(new PageImpl<>(ImmutableList.of("a"))));

    assertThat(Merger.mergeMaps(maps), is(maps.get(0)));
    assertThat(Merger.mergeArrays(arrays), is(arrays.get(0)));
    assertThat(Merger.mergePages(pages), is(pages.get(0)));
  }

  @Test
  public void shouldMergeMaps() {
    Map<String, String> map1 = ImmutableMap.of("a", "b");
    Map<String, String> map2 = ImmutableMap.of("b", "c");
    Map<String, String> map3 = ImmutableMap.of("c", "d");
    Map<String, String> merged = Merger.mergeMaps(ImmutableList.of(map1, map2, map3));

    assertThat(merged, is(notNullValue()));
    assertThat(merged.entrySet(), hasSize(3));
    assertThat(merged, hasEntry("a", "b"));
    assertThat(merged, hasEntry("b", "c"));
    assertThat(merged, hasEntry("c", "d"));
  }

  @Test
  public void shouldMergeArrays() {
    String[] array1 = new String[]{"a", "b"};
    String[] array2 = new String[]{"b", "c"};
    String[] array3 = new String[]{"c", "d"};
    String[] merged = Merger.mergeArrays(ImmutableList.of(array1, array2, array3));

    assertThat(merged, is(notNullValue()));
    assertThat(merged.length, is(4));
    assertThat(merged, arrayContaining("a", "b", "c", "d"));
  }

  @Test
  public void shouldMergePages() {
    PageImplRepresentation<String> page1 = new PageImplRepresentation<>(
        new PageImpl<>(ImmutableList.of("a")));
    PageImplRepresentation<String> page2 = new PageImplRepresentation<>(
        new PageImpl<>(ImmutableList.of("b", "d")));
    PageImplRepresentation<String> page3 = new PageImplRepresentation<>(
        new PageImpl<>(ImmutableList.of("c")));
    PageImplRepresentation<String> merged = Merger
        .mergePages(ImmutableList.of(page1, page2, page3));

    assertThat(merged, is(notNullValue()));
    assertThat(merged.getContent(), hasSize(4));
    assertThat(merged.getContent(), hasItems("a", "b", "c", "d"));
  }
}
