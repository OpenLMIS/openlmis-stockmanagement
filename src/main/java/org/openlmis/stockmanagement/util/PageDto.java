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

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PageDto offers a convenient substitute for PageImpl. Because the former lacks a default
 * constructor, it is inconvenient to deserialize. PageDto may be used in its stead.
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class PageDto<T> implements Page<T> {
  private boolean last;
  private boolean first;

  private int totalPages;
  private long totalElements;
  private int size;
  private int number;
  private int numberOfElements;

  @JsonDeserialize(using = CustomSortDeserializer.class)
  private Sort sort;

  private List<T> content;

  public PageDto() {
    this(new PageImpl<>(new ArrayList<>()));
  }

  /**
   * Creates new instance based on data from {@link Page} instance.
   */
  public PageDto(Page<T> page) {
    this(
        checkNotNull(page).isLast(), page.isFirst(), page.getTotalPages(),
        page.getTotalElements(), page.getSize(), page.getNumber(),
        page.getNumberOfElements(), page.getSort(), checkNotNull(page.getContent())
    );
  }

  @Override
  public boolean hasContent() {
    return !content.isEmpty();
  }

  @Override
  public boolean hasNext() {
    return !last;
  }

  @Override
  public boolean hasPrevious() {
    return !first;
  }

  @Override
  public Pageable nextPageable() {
    return hasNext() ? PageRequest.of(number + 1, size, sort) : null;
  }

  @Override
  public Pageable previousPageable() {
    return hasPrevious()
        ? PageRequest.of(number - 1, size, sort)
        : PageRequest.of(0, size, sort);
  }

  @Override
  public <S> Page<S> map(Function<? super T, ? extends S> converter) {
    checkNotNull(converter);

    List<S> result = content.stream().map(converter).collect(Collectors.toList());
    Pageable pageable = PageRequest.of(number, size, sort);
    Page<S> page = new PageImpl<>(result, pageable, totalElements);

    return new PageDto<>(page);
  }

  @Override
  public Iterator<T> iterator() {
    return content.iterator();
  }
}
