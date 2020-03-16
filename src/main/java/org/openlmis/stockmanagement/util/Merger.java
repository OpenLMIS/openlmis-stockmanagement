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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Getter(AccessLevel.PACKAGE)
// we keep implementation classes inside this one to give a single access point by ofXXX methods.
@SuppressWarnings("PMD.TooManyMethods")
public abstract class Merger<T> {
  private List<T> elements;
  private Supplier<T> defaultValue;

  private Merger(List<T> elements) {
    this.elements = elements;
    this.defaultValue = () -> null;
  }

  public static <K, V> Merger<Map<K, V>> ofMaps(List<Map<K, V>> elements) {
    return of(elements).orElseGet(() -> new MapsMerger<>(elements));
  }

  public static <E> Merger<E[]> ofArrays(List<E[]> elements) {
    return of(elements).orElseGet(() -> new ArraysMerger<>(elements));
  }

  public static <E> Merger<PageDto<E>> ofPages(
      List<PageDto<E>> elements) {
    return of(elements).orElseGet(() -> new PageMerger<>(elements));
  }

  private static <E> Optional<Merger<E>> of(List<E> elements) {
    if (CollectionUtils.isEmpty(elements)) {
      return Optional.of(new EmptyMerger<>());
    }

    if (elements.size() == 1) {
      return Optional.of(new SingleMerger<>(elements));
    }

    return Optional.empty();
  }

  public Merger<T> withDefaultValue(Supplier<T> defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public abstract T merge();

  private static final class EmptyMerger<T> extends Merger<T> {

    private EmptyMerger() {
      super(Collections.emptyList());
    }

    @Override
    public T merge() {
      return getDefaultValue().get();
    }

  }

  private static final class SingleMerger<T> extends Merger<T> {

    private SingleMerger(List<T> elements) {
      super(elements);
    }

    @Override
    public T merge() {
      return getElements().get(0);
    }
  }


  private static final class MapsMerger<K, V> extends Merger<Map<K, V>> {

    private MapsMerger(List<Map<K, V>> elements) {
      super(elements);
    }

    @Override
    public Map<K, V> merge() {
      return getElements()
          .stream()
          .filter(Objects::nonNull)
          .map(Map::entrySet)
          .flatMap(Collection::stream)
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

  }

  private static final class ArraysMerger<T> extends Merger<T[]> {

    private ArraysMerger(List<T[]> elements) {
      super(elements);
    }

    @Override
    public T[] merge() {
      return getElements()
          .stream()
          .filter(Objects::nonNull)
          .flatMap(Arrays::stream)
          .distinct()
          .toArray(size -> {
            Class<?> componentType = getElements().get(0).getClass().getComponentType();
            return (T[]) Array.newInstance(componentType, size);
          });
    }
  }

  private static final class PageMerger<T> extends Merger<PageDto<T>> {

    private PageMerger(List<PageDto<T>> elements) {
      super(elements);
    }

    @Override
    public PageDto<T> merge() {
      List<T> content = getElements()
          .stream()
          .filter(Objects::nonNull)
          .map(PageDto::getContent)
          .flatMap(Collection::stream)
          .distinct()
          .collect(Collectors.toList());

      Page<T> page = Pagination.getPage(content, PageRequest.of(0, content.size()));
      return new PageDto<>(page);
    }
  }

}
