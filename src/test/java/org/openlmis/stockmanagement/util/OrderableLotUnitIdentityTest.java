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

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.identity.OrderableLotUnitIdentity;

public class OrderableLotUnitIdentityTest {
  @Test
  public void sameOrderableAndLotIdShouldEqualAndHaveSameHash() throws Exception {
    //given
    OrderableLotUnitIdentity identity1 = new OrderableLotUnitIdentity(randomUUID(), randomUUID(),
        randomUUID());

    OrderableLotUnitIdentity identity2 = new OrderableLotUnitIdentity(
        fromString(identity1.getOrderableId().toString()),
        fromString(identity1.getLotId().toString()),
        fromString(identity1.getUnitOfOrderableId().toString()));

    //when
    boolean equals = identity1.equals(identity2);
    int identity1Hash = identity1.hashCode();
    int identity2Hash = identity2.hashCode();

    //then
    assertTrue(equals);
    assertEquals(identity1Hash, identity2Hash);
  }
}