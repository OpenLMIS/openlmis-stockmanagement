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

package org.openlmis.stockmanagement.service;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Test;

public class RequestHeadersTest {

  @Test
  public void shouldSetParameter() throws Exception {
    RequestHeaders params = RequestHeaders
        .init()
        .set("a", "b")
        .setAuth("token");

    Map<String, Object> map = toMap(params);
    assertThat(map, hasEntry("a", "b"));
    assertThat(map, hasEntry(AUTHORIZATION, "Bearer token"));
  }

  @Test
  public void shouldNotSetParametersValueIsNull() throws Exception {
    RequestHeaders params = RequestHeaders
        .init()
        .set("a", null)
        .setAuth(null);

    Map<String, Object> map = toMap(params);
    assertThat(map, not(hasKey("a")));
    assertThat(map, not(hasKey(AUTHORIZATION)));
  }

  @Test
  public void shouldSetAllParametersFromOtherInstance() throws Exception {
    RequestHeaders parent = RequestHeaders.init().set("a", "b");
    RequestHeaders params = RequestHeaders.init().setAll(parent);

    assertThat(toMap(params), hasEntry("a", "b"));
  }

  private Map<String, Object> toMap(RequestHeaders parameters) {
    Map<String, Object> map = Maps.newHashMap();
    parameters.forEach(e -> map.put(e.getKey(), e.getValue()));

    return map;
  }
}
