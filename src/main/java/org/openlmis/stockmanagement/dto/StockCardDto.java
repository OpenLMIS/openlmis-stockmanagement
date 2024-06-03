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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public final class StockCardDto implements IdentifiableByOrderableLot {

  @JsonInclude(NON_NULL)
  private UUID id;

  private Integer stockOnHand;
  private FacilityDto facility;
  private ProgramDto program;
  private OrderableDto orderable;
  private LotDto lot;
  private Map<String, String> extraData;
  private boolean isActive;

  @JsonFormat(shape = STRING)
  private LocalDate lastUpdate;

  @JsonInclude(NON_NULL)
  private List<StockCardLineItemDto> lineItems;

  @JsonIgnore
  public UUID getOrderableId() {
    return orderable.getId();
  }

  @JsonIgnore
  public UUID getLotId() {
    return lot == null ? null : lot.getId();
  }

  public boolean hasLot() {
    return getLotId() != null;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  /**
   * Create stock card dto from stock card.
   *
   * @param stockCard stock card.
   * @return the created stock card dto.
   */
  public static StockCardDto createFrom(StockCard stockCard) {

    return StockCardDto.builder()
        .id(stockCard.getId())
        .lineItems(stockCard.getLineItems().stream()
            .map(StockCardLineItemDto::createFrom).collect(toList()))
        .stockOnHand(stockCard.getStockOnHand())
        .isActive(stockCard.isActive())
        .build();
  }
}
