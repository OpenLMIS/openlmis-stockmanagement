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

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.springframework.data.domain.Sort;

public class CustomSortDeserializerTest {

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void shouldDeserializeArraySort() throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode order = mapper.createObjectNode();
    order.put("direction", "DESC");
    order.put("property", "startDate");
    order.put("ignoreCase", false);
    order.put("nullHandling", "NATIVE");
    order.put("ascending", false);
    order.put("descending", true);

    ArrayNode arrayNode = mapper.createArrayNode();
    arrayNode.add(order);

    ObjectNode testObject = mapper.createObjectNode();
    testObject.set("sort", arrayNode);

    Sort sort = deserialize(testObject.toString());

    assertEquals(Sort.Direction.DESC, sort.getOrderFor("startDate").getDirection());
  }

  private Sort deserialize(String json) throws IOException {
    TestObject testObject = mapper.readValue(json, TestObject.class);
    return testObject.getSort();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  private static class TestObject {

    private Sort sort;

    public Sort getSort() {
      return sort;
    }

    @JsonDeserialize(using = CustomSortDeserializer.class)
    public void setSort(Sort sort) {
      this.sort = sort;
    }
  }
}
