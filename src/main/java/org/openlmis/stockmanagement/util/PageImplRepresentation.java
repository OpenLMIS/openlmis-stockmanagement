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

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * PageImplRepresentation offers a convenient substitute for PageImpl.
 * Because the former lacks a default constructor, it is inconvenient to
 * deserialize. PageImplRepresentation may be used in its stead.
 */
public class PageImplRepresentation<T> extends PageImpl<T> {

  private static final long serialVersionUID = 1L;
  private boolean last;
  private boolean first;

  private int totalPages;
  private long totalElements;
  private int size;
  private int number;
  private int numberOfElements;

  private Sort sort;

  private List<T> content;

  public PageImplRepresentation() {
    this(new PageImpl<>(Lists.newArrayList()));
  }

  /**
   * Creates new instance based on data from {@link Page} instance.
   */
  public PageImplRepresentation(Page<T> page) {
    super(page.getContent());
    this.last = page.isLast();
    this.first = page.isFirst();
    this.totalPages = page.getTotalPages();
    this.totalElements = page.getTotalElements();
    this.size = page.getSize();
    this.number = page.getNumber();
    this.numberOfElements = page.getNumberOfElements();
    this.sort = page.getSort();
    this.content = page.getContent();
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public int getNumberOfElements() {
    return numberOfElements;
  }

  public void setNumberOfElements(int numberOfElements) {
    this.numberOfElements = numberOfElements;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }


  public boolean isFirst() {
    return first;
  }

  public void setFirst(boolean first) {
    this.first = first;
  }

  public boolean isLast() {
    return last;
  }

  public void setLast(boolean last) {
    this.last = last;
  }

  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
  }

  public Sort getSort() {
    return sort;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  public PageImpl<T> pageImpl() {
    return new PageImpl<T>(getContent(), new PageRequest(getNumber(),
            getSize(), getSort()), getTotalElements());
  }
}
