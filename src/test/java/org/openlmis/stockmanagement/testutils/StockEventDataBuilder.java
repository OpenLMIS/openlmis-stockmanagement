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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;

public class StockEventDataBuilder {
  private UUID id = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID userId = UUID.randomUUID();
  private ZonedDateTime processedDate = ZonedDateTime.now();
  private boolean isActive = true;
  private String signature;
  private String documentNumber;
  private List<StockEventLineItem> lineItems = Lists.newArrayList();

  public StockEventDataBuilder withoutId() {
    id = null;
    return this;
  }

  public StockEventDataBuilder withFacility(UUID facility) {
    this.facilityId = facility;
    return this;
  }

  public StockEventDataBuilder withProgram(UUID program) {
    this.programId = program;
    return this;
  }

  public StockEventDataBuilder withisActive(boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * Creates stock event based on parameters from the builder.
   */
  public StockEvent build() {
    StockEvent event = new StockEvent(
        facilityId, programId, userId,processedDate, isActive, signature, documentNumber, lineItems
    );
    event.setId(id);

    return event;
  }
}
