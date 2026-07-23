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

package org.openlmis.stockmanagement.domain.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

public class StockEventLineItemTest {

  @Test
  public void issueShouldBeMovement() {
    StockEventLineItem issue = StockEventLineItem.builder()
        .destinationId(UUID.randomUUID())
        .build();

    assertTrue(issue.isIssue());
    assertFalse(issue.isReceive());
    assertTrue(issue.isMovement());
  }

  @Test
  public void receiveShouldBeMovement() {
    StockEventLineItem receive = StockEventLineItem.builder()
        .sourceId(UUID.randomUUID())
        .build();

    assertFalse(receive.isIssue());
    assertTrue(receive.isReceive());
    assertTrue(receive.isMovement());
  }

  @Test
  public void adjustmentShouldBeNeitherIssueNorReceive() {
    StockEventLineItem adjustment = StockEventLineItem.builder()
        .reasonId(UUID.randomUUID())
        .build();

    assertFalse(adjustment.isIssue());
    assertFalse(adjustment.isReceive());
    assertFalse(adjustment.isMovement());
  }
}
