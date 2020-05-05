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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class CustomSortSerializerTest {

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void serializeShouldSerializeSort() throws JsonProcessingException {
    TestObject testObject = new TestObject(Sort.by(Direction.DESC, "startDate"));
    String json = mapper.writeValueAsString(testObject);

    assertThat(json, containsString("\"direction\":\"DESC\""));
    assertThat(json, containsString("\"property\":\"startDate\""));
  }

  @AllArgsConstructor
  private static class TestObject {

    private Sort sort;

    @JsonSerialize(using = CustomSortSerializer.class)
    public Sort getSort() {
      return sort;
    }

    public void setSort(Sort sort) {
      this.sort = sort;
    }
  }
}
