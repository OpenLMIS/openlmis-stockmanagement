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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEventHistoryDto {

  private UUID id;
  private String documentNumber;
  private EventOrigin type;
  private String signature;
  private LocalDate occurredDate;
  private ZonedDateTime processedDate;
  private Integer entriesCount;
  private UUID userId;
  private String username;

  /**
   * Creates a history row DTO from a stock event's scalar fields. The line-item-derived fields
   * ({@code entriesCount}, {@code occurredDate}) and {@code username} are filled in afterwards
   * by the service.
   */
  public static StockEventHistoryDto newInstance(StockEvent event) {
    return StockEventHistoryDto.builder()
        .id(event.getId())
        .documentNumber(event.getDocumentNumber())
        .type(event.getEventOrigin())
        .signature(event.getSignature())
        .userId(event.getUserId())
        .processedDate(event.getProcessedDate())
        .build();
  }
}
