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

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.openlmis.stockmanagement.util.UuidUtil.ID;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class UuidUtilTest {

  @Test
  public void shouldRetrieveIdsFromMap() {
    UUID id1 = randomUUID();
    UUID id2 = randomUUID();
    UUID id3 = randomUUID();

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    params.add(ID, id1.toString());
    params.add(ID, id2.toString());
    params.add(ID, id3.toString());
    params.add("ids", randomUUID().toString());
    params.add("someParameter", randomUUID().toString());

    assertThat(UuidUtil.getIds(params), hasItems(id1, id2, id3));
  }

  @Test
  public void shouldReturnEmptyListForNullMap() {
    assertEquals(Collections.emptySet(), UuidUtil.getIds(null));
  }

  @Test
  public void shouldReturnEmptyOptionalForInvalidUuidString() {
    assertEquals(Optional.empty(), UuidUtil.fromString("invalid-uuid-string"));
  }

  @Test
  public void shouldReturnEmptyOptionalForNullUuidString() {
    assertEquals(Optional.empty(), UuidUtil.fromString(null));
  }

  @Test
  public void shouldReturnOptionalForUuidString() {
    UUID id = randomUUID();
    assertEquals(Optional.of(id), UuidUtil.fromString(id.toString()));
  }
}
