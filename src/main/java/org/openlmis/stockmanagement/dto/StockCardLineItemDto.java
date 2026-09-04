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

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;

@Builder
@Data
public class StockCardLineItemDto {

  @JsonUnwrapped
  private StockCardLineItem lineItem;

  private FacilityDto source;
  private FacilityDto destination;

  private UUID originEventId;
  private EventOrigin eventOrigin;

  // Set on read for a cancellation line: the original event it reverses (the "Reversing" column).
  private UUID reversedEventId;
  private String reversedEventDocumentNumber;

  // Set on read for an original line: the cancellation event that reversed it ("Reversed by").
  private UUID cancellationEventId;
  private String cancellationEventDocumentNumber;

  // The stock event line item this row was created from: a stable, unique id the
  // reverse view uses to cancel the exact line, and the resolver uses as the cross-link key.
  private UUID stockEventLineItemId;

  /**
   * Create stock card line item dto from stock card line item.
   *
   * @param stockCardLineItem stock card line item.
   * @return the created stock card line item dto.
   */
  public static StockCardLineItemDto createFrom(StockCardLineItem stockCardLineItem) {
    StockEvent originEvent = stockCardLineItem.getOriginEvent();
    return StockCardLineItemDto.builder()
        .lineItem(stockCardLineItem)
        .originEventId(originEvent == null ? null : originEvent.getId())
        .eventOrigin(originEvent == null ? null : originEvent.getEventOrigin())
        .stockEventLineItemId(stockCardLineItem.getOriginEventLineItemId())
        .build();
  }
}
