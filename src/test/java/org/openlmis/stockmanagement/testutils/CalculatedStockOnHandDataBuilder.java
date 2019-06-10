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

import java.time.LocalDate;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;

public class CalculatedStockOnHandDataBuilder {
  private UUID id;
  private StockCard stockCard;
  private LocalDate date;
  private Integer stockOnHand;

  /**
   * Creates builder for creating new instance of {@link CalculatedStockOnHand}.
   */
  public CalculatedStockOnHandDataBuilder() {
    this.id = UUID.randomUUID();
    this.date = LocalDate.now();
    this.stockOnHand = 15;
  }

  public CalculatedStockOnHandDataBuilder withoutId() {
    this.id = null;
    return this;
  }

  public CalculatedStockOnHandDataBuilder withStockCard(StockCard stockCard) {
    this.stockCard = stockCard;
    return this;
  }

  /**
   * Creates calculated stock on hand based on parameters from the builder.
   */
  public CalculatedStockOnHand build() {
    CalculatedStockOnHand result = new CalculatedStockOnHand(
            stockOnHand, stockCard, date
    );
    result.setId(id);

    return result;
  }
}
