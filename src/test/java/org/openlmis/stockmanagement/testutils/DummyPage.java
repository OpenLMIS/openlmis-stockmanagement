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

package org.openlmis.stockmanagement.testutils;

import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
public class DummyPage<E> implements Page {
  private List<E> content;

  @Override
  public int getTotalPages() {
    return 0;
  }

  @Override
  public long getTotalElements() {
    return 0;
  }

  @Override
  public Page map(Converter converter) {
    return null;
  }

  @Override
  public int getNumber() {
    return 0;
  }

  @Override
  public int getSize() {
    return 0;
  }

  @Override
  public int getNumberOfElements() {
    return 0;
  }

  @Override
  public boolean hasContent() {
    return false;
  }

  @Override
  public Sort getSort() {
    return null;
  }

  @Override
  public boolean isFirst() {
    return false;
  }

  @Override
  public boolean isLast() {
    return false;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public boolean hasPrevious() {
    return false;
  }

  @Override
  public Pageable nextPageable() {
    return null;
  }

  @Override
  public Pageable previousPageable() {
    return null;
  }

  @Override
  public Iterator iterator() {
    return null;
  }
}
