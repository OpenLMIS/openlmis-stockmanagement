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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEventLineDetailDto {

  private OrderableDto orderable;
  private LotDto lot;
  private FacilityDto source;
  private FacilityDto destination;
  private Integer quantity;
  private LocalDate occurredDate;
  private StockCardLineItemReason reason;
  private Integer stockOnHand;
  private String documentNumber;

  /**
   * Flattens one resolved stock card line item (with its parent card's product/lot) into a single
   * transaction-history detail row.
   *
   * @param card     the resolved stock card the line item belongs to (carries orderable + lot).
   * @param lineItem the resolved line item DTO (carries SOH, source/destination, reason).
   * @return the detail row.
   */
  public static StockEventLineDetailDto newInstance(StockCardDto card,
      StockCardLineItemDto lineItem) {
    StockCardLineItem item = lineItem.getLineItem();
    return StockEventLineDetailDto.builder()
        .orderable(card.getOrderable())
        .lot(card.getLot())
        .source(lineItem.getSource())
        .destination(lineItem.getDestination())
        .quantity(item.getQuantity())
        .occurredDate(item.getOccurredDate())
        .reason(item.getReason())
        .stockOnHand(item.getStockOnHand())
        .documentNumber(item.getDocumentNumber())
        .build();
  }
}
