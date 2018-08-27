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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.DispensableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;

public class OrderableDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String productCode;
  private String fullProductName;
  private Long netContent;
  private DispensableDto dispensable;
  private Map<String, String> identifiers;
  private Map<String, String> extraData;

  /**
   * Creates builder for creating new instance of {@link OrderableDtoDataBuilder}.
   */
  public OrderableDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    productCode = "P" + instanceNumber;
    fullProductName = "Product " + instanceNumber;
    netContent = 10L;
    dispensable = new DispensableDto("pack", "Pack");
    identifiers = new HashMap<>();
    extraData = null;
  }

  /**
   * Creates new instance of {@link OrderableDto} with properties.
   * @return created orderable.
   */
  public OrderableDto build() {
    return new OrderableDto(
        id, productCode, fullProductName, netContent, dispensable, identifiers, extraData
    );
  }

  public OrderableDtoDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }
}
