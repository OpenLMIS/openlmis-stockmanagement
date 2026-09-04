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

package org.openlmis.stockmanagement.dto;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Addresses a lot on a stock event line item by its code and expiry, as an alternative to a
 * {@code lotId}. During event processing the code is resolved against existing lots for the line's
 * trade item and reused, or (where the event shape permits) the lot is created.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockEventLineItemLotDto {
  private String lotCode;

  @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
  private LocalDate expirationDate;
}
