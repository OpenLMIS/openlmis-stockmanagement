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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@EqualsAndHashCode
public final class RequestParameters {
  private final MultiValueMap<String, String> params;

  private RequestParameters() {
    params = new LinkedMultiValueMap<>();
  }

  public static RequestParameters init() {
    return new RequestParameters();
  }

  /**
   * Constructs new RequestParameters based on Map with request parameters.
   */
  public static RequestParameters of(Map<String, Object> params) {
    RequestParameters requestParameters = new RequestParameters();
    params.forEach(requestParameters::set);
    return requestParameters;
  }

  /**
   * Set parameter (key argument) with the value only if the value is not null.
   */
  public RequestParameters set(String key, Collection<?> valueCollection) {
    Optional
        .ofNullable(valueCollection)
        .orElse(Collections.emptyList())
        .forEach(elem -> set(key, elem));

    return this;
  }

  /**
   * Set parameter (key argument) with the value only if the value is not null.
   */
  public RequestParameters set(String key, Object value) {
    if (null != value) {
      params.add(key, String.valueOf(value));
    }

    return this;
  }

  /**
   * Set paging parameters from {@code pageable}.
   *
   * @param pageable the object to get pagination parameters from, not null
   * @return this
   */
  public RequestParameters set(Pageable pageable) {
    requireNonNull(pageable);
    params.set("size", String.valueOf(pageable.getPageSize()));
    params.set("page", String.valueOf(pageable.getPageNumber()));
    params.set("sort", pageable.getSort().toString());
    return this;
  }

  /**
   * Copy parameters from the existing {@link RequestParameters}. If null value has been passed,
   * the method will return non changed instance.
   */
  public RequestParameters setAll(RequestParameters parameters) {
    if (null != parameters) {
      parameters.forEach(entry -> set(entry.getKey(), entry.getValue()));
    }

    return this;
  }

  public void forEach(Consumer<Map.Entry<String, List<String>>> action) {
    params.entrySet().forEach(action);
  }

  /**
   * Split this request parameters into two smaller chunks.
   */
  public Pair<RequestParameters, RequestParameters> split() {
    if (params.isEmpty()) {
      return Pair.of(this, null);
    }

    Set<Map.Entry<String, List<String>>> entries = params.entrySet();

    if (entries.stream().noneMatch(entry -> entry.getValue().size() > 1)) {
      return Pair.of(this, null);
    }

    Map.Entry<String, List<String>> max = entries.iterator().next();
    for (Map.Entry<String, List<String>> entry : entries) {
      if (entry.getValue().size() > max.getValue().size()) {
        max = entry;
      }
    }

    RequestParameters left = init().setAll(this);
    RequestParameters right = init().setAll(this);

    left.params.remove(max.getKey());
    right.params.remove(max.getKey());

    List<String> list = max.getValue();
    int chunkSize = list.size() / 2;
    int leftOver = list.size() % 2;

    List<String> leftCollection = list.subList(0, chunkSize + leftOver);
    List<String> rightCollection = list.subList(chunkSize + leftOver, list.size());

    left.set(max.getKey(), leftCollection);
    right.set(max.getKey(), rightCollection);

    return Pair.of(left, right);
  }
}
