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
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.stream.Collectors.toList;
import static org.openlmis.stockmanagement.util.OrderableAndLotIdToString.idsToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.openlmis.stockmanagement.domain.card.StockCard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCardDto {

  @JsonInclude(NON_NULL)
  private UUID id;

  private Integer stockOnHand;
  private FacilityDto facility;
  private ProgramDto program;
  private OrderableDto orderable;
  private LotDto lot;

  @JsonFormat(shape = STRING)
  private ZonedDateTime lastUpdate;

  @JsonInclude(NON_NULL)
  private List<StockCardLineItemDto> lineItems;

  /**
   * Create stock card dto from stock card.
   *
   * @param stockCard stock card.
   * @return the created stock card dto.
   */
  public static StockCardDto createFrom(StockCard stockCard) {
    List<StockCardLineItemDto> lineItemDtos = stockCard.getLineItems().stream()
        .map(StockCardLineItemDto::createFrom).collect(toList());

    return StockCardDto.builder()
        .id(stockCard.getId())
        .lineItems(lineItemDtos)
        .stockOnHand(stockCard.getStockOnHand())
        .build();
  }

  public String orderableAndLotString() {
    return idsToString(orderable.getId(), lot == null ? null : lot.getId());
  }
}
