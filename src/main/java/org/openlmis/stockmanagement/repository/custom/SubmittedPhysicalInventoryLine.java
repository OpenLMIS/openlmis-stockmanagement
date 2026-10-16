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

package org.openlmis.stockmanagement.repository.custom;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Typed carrier for one counted product (and lot) of a submitted physical inventory, with what is
 * needed to order the inventory against a movement on the stock card.
 */
@Getter
@AllArgsConstructor
public class SubmittedPhysicalInventoryLine {

  private final UUID physicalInventoryId;
  private final LocalDate occurredDate;
  private final String documentNumber;
  // Processed date of the inventory's stock event; null when the inventory has none.
  private final ZonedDateTime processedDate;
  private final UUID orderableId;
  private final UUID lotId;
}
