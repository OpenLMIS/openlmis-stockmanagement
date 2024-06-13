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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
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

  @Test
  public void shouldNotBeEqualDueToOrderableId() {
    //given
    UUID orderableId1 = UUID.randomUUID();
    UUID orderableId2 = UUID.randomUUID();

    UUID lotId = UUID.randomUUID();
    UUID unitOfOrderableId = UUID.randomUUID();

    OrderableLotUnitIdentity orderableLotUnitIdentity1 =
        new OrderableLotUnitIdentity(
            orderableId1,
            lotId,
            unitOfOrderableId
        );
    OrderableLotUnitIdentity orderableLotUnitIdentity2 =
        new OrderableLotUnitIdentity(
            orderableId2,
            lotId,
            unitOfOrderableId
        );

    //when
    boolean areEqual =
        orderableLotUnitIdentity1.equals(orderableLotUnitIdentity2);

    //then
    assertFalse(areEqual);
  }

  @Test
  public void shouldNotBeEqualDueToLotId() {
    //given
    UUID orderableId = UUID.randomUUID();

    UUID lotId1 = UUID.randomUUID();
    UUID lotId2 = UUID.randomUUID();

    UUID unitOfOrderableId = UUID.randomUUID();

    OrderableLotUnitIdentity orderableLotUnitIdentity1 =
        new OrderableLotUnitIdentity(
            orderableId,
            lotId1,
            unitOfOrderableId
        );
    OrderableLotUnitIdentity orderableLotUnitIdentity2 =
        new OrderableLotUnitIdentity(
            orderableId,
            lotId2,
            unitOfOrderableId
        );

    //when
    boolean areEqual =
        orderableLotUnitIdentity1.equals(orderableLotUnitIdentity2);

    //then
    assertFalse(areEqual);
  }

  @Test
  public void shouldNotBeEqualDueToUnitOfOrderableId() {
    //given
    UUID orderableId = UUID.randomUUID();
    UUID lotId = UUID.randomUUID();
    UUID unitOfOrderableId1 = UUID.randomUUID();
    UUID unitOfOrderableId2 = UUID.randomUUID();

    OrderableLotUnitIdentity orderableLotUnitIdentity1 =
        new OrderableLotUnitIdentity(
            orderableId,
            lotId,
            unitOfOrderableId1
        );
    OrderableLotUnitIdentity orderableLotUnitIdentity2 =
        new OrderableLotUnitIdentity(
            orderableId,
            lotId,
            unitOfOrderableId2
        );

    //when
    boolean areEqual =
        orderableLotUnitIdentity1.equals(orderableLotUnitIdentity2);

    //then
    assertFalse(areEqual);
  }

  @Test
  public void shouldNotBeEqualDueToSecondIdentityIsNull() {
    //given
    UUID orderableId = UUID.randomUUID();
    UUID lotId = UUID.randomUUID();
    UUID unitOfOrderableId = UUID.randomUUID();

    OrderableLotUnitIdentity orderableLotUnitIdentity1 =
        new OrderableLotUnitIdentity(
            orderableId,
            lotId,
            unitOfOrderableId
        );
    OrderableLotUnitIdentity orderableLotUnitIdentity2 = null;

    //when
    boolean areEqual =
        orderableLotUnitIdentity1.equals(orderableLotUnitIdentity2);

    //then
    assertFalse(areEqual);
  }
}
