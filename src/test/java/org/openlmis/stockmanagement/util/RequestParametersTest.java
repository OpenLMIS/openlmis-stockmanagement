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

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

@SuppressWarnings("PMD.TooManyMethods")
public class RequestParametersTest {
  private static final String KEY = "key";
  private static final String VALUE = "value";

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(RequestParameters.class)
        .verify();
  }

  @Test
  public void shouldConstructFromMap() {
    HashMap<String, Object> map = new HashMap<>();
    map.put(KEY, VALUE);

    RequestParameters requestParameters = RequestParameters.of(map);
    assertThat(toMap(requestParameters), hasEntry(KEY, Collections.singletonList(VALUE)));
  }

  @Test
  public void shouldSetParameter() {
    RequestParameters params = RequestParameters.init().set(KEY, VALUE);
    assertThat(toMap(params), hasEntry(KEY, Collections.singletonList(VALUE)));
  }

  @Test
  public void shouldNotSetParametersValueCollectionIsNull() {
    RequestParameters params = RequestParameters.init().set(KEY, null);
    assertThat(toMap(params), not(hasKey(KEY)));
  }

  @Test
  public void shouldNotSetParametersValueIsNull() {
    RequestParameters params = RequestParameters.init().set(KEY, (Object) null);
    assertThat(toMap(params), not(hasKey(KEY)));
  }

  @Test
  public void shouldSetAllParametersFromOtherInstance() {
    RequestParameters parent = RequestParameters.init().set(KEY, VALUE);
    RequestParameters params = RequestParameters.init().setAll(parent);

    assertThat(toMap(params), hasEntry(KEY, Collections.singletonList(VALUE)));
  }

  @Test
  public void shouldHandleNullValueWhenTryToSetAllParametersFromOtherInstance() {
    RequestParameters params = RequestParameters.init().setAll(null);

    assertThat(toMap(params).entrySet(), hasSize(0));
  }

  @Test
  public void shouldSplit() {
    RequestParameters params = RequestParameters
        .init()
        .set(KEY, range(0, 10).mapToObj(String::valueOf).collect(toList()));

    Pair<RequestParameters, RequestParameters> split = params.split();

    assertThat(split.getLeft(), is(notNullValue()));
    assertThat(split.getRight(), is(notNullValue()));

    Set<Object> values0 = new HashSet<>();
    Set<Object> values1 = new HashSet<>();

    split.getLeft().forEach(entry -> {
      assertThat(entry.getKey(), is(KEY));
      values0.addAll(entry.getValue());
    });

    split.getRight().forEach(entry -> {
      assertThat(entry.getKey(), is(KEY));
      values1.addAll(entry.getValue());
    });


    assertThat(values0, hasSize(5));
    assertThat(values1, hasSize(5));

    // the values0 will contain only values that are in the values1.
    values0.retainAll(values1);

    assertThat(values0, hasSize(0));
  }

  @Test
  public void shouldSplitByFirstBiggestSet() {
    RequestParameters params = RequestParameters
        .init()
        .set(KEY, range(0, 10).mapToObj(String::valueOf).collect(toList()))
        .set(VALUE, range(0, 11).mapToObj(String::valueOf).collect(toList()));

    Pair<RequestParameters, RequestParameters> split = params.split();

    assertThat(split.getLeft(), is(notNullValue()));
    assertThat(split.getRight(), is(notNullValue()));

    Map<String, List<String>> map0 = toMap(split.getLeft());
    assertThat(map0, hasEntry(is(KEY), hasSize(10)));
    assertThat(map0, hasEntry(is(VALUE), hasSize(6)));

    Map<String, List<String>> map1 = toMap(split.getRight());
    assertThat(map1, hasEntry(is(KEY), hasSize(10)));
    assertThat(map1, hasEntry(is(VALUE), hasSize(5)));
  }

  @Test
  public void shouldNotSplitWhenListHasOneElement() {
    RequestParameters params = RequestParameters.init().set(KEY, VALUE);
    Pair<RequestParameters, RequestParameters> split = params.split();

    assertThat(split.getLeft(), is(params));
    assertThat(split.getRight(), is(nullValue()));
  }

  @Test
  public void shouldNotSplitWhenObjectIsEmpty() {
    RequestParameters params = RequestParameters.init();
    Pair<RequestParameters, RequestParameters> split = params.split();

    assertThat(split.getLeft(), is(params));
    assertThat(split.getRight(), is(nullValue()));
  }


  private Map<String, List<String>> toMap(RequestParameters parameters) {
    Map<String, List<String>> map = Maps.newHashMap();
    parameters.forEach(e -> map.put(e.getKey(), e.getValue()));

    return map;
  }
}
