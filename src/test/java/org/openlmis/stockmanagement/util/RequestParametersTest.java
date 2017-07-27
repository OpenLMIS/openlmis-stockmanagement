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

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Maps;
import org.junit.Test;
import java.util.Map;

public class RequestParametersTest {

  @Test
  public void shouldSetParameter() throws Exception {
    RequestParameters params = RequestParameters.init().set("a", "b");
    assertThat(toMap(params), hasEntry("a", "b"));
  }

  @Test
  public void shouldNotSetParametersValueIsNull() throws Exception {
    RequestParameters params = RequestParameters.init().set("a", null);
    assertThat(toMap(params), not(hasKey("a")));
  }

  @Test
  public void shouldSetAllParametersFromOtherInstance() throws Exception {
    RequestParameters parent = RequestParameters.init().set("a", "b");
    RequestParameters params = RequestParameters.init().setAll(parent);

    assertThat(toMap(params), hasEntry("a", "b"));
  }

  private Map<String, Object> toMap(RequestParameters parameters) {
    Map<String, Object> map = Maps.newHashMap();
    parameters.forEach(e -> map.put(e.getKey(), e.getValue()));

    return map;
  }
}