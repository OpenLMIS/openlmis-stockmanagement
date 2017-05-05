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

package org.openlmis.stockmanagement.utils;

import org.openlmis.stockmanagement.dto.referencedata.ResultDto;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Extension of {@link ParameterizedTypeReference} from Spring that allows
 * dynamically changing the type it represents at runtime. Since generic
 * hacks are generally ugly, so is this class. It eases the usage of the rest
 * template however, allowing easily retrieving {@link ResultDto} objects with
 * the provided generic type at runtime.
 */
public class DynamicParametrizedTypeReference<T>
    extends ParameterizedTypeReference<ResultDto<T>> {
  private final Class<?> valueType;

  /**
   * Constructs an instance that will represents {@link ResultDto} wrappers for
   * the given type.
   *
   * @param valueType the value type (generic type) of the {@link ResultDto} type that this will
   *                  represent
   */
  public DynamicParametrizedTypeReference(Class<?> valueType) {
    this.valueType = valueType;
  }

  @Override
  public Type getType() {
    Type[] responseWrapperActualTypes = {valueType};

    return new ParameterizedType() {
      @Override
      public Type[] getActualTypeArguments() {
        return responseWrapperActualTypes;
      }

      @Override
      public Type getRawType() {
        return ResultDto.class;
      }

      @Override
      public Type getOwnerType() {
        return null;
      }
    };
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DynamicParametrizedTypeReference)) {
      return false;
    }

    DynamicParametrizedTypeReference dptr = (DynamicParametrizedTypeReference) other;

    return getType().equals(dptr.getType());
  }

  @Override
  public int hashCode() {
    return getType().hashCode();
  }
}
