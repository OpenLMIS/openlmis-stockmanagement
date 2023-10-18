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
import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StockEventLineItemDto implements IdentifiableByOrderableLot, VvmApplicable {
  private UUID orderableId;
  private UUID lotId;
  private Integer quantity;
  private Map<String, String> extraData;
  @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
  private LocalDate occurredDate;
  private UUID reasonId;
  private String reasonFreeText;
  private UUID sourceId;
  private String sourceFreeText;
  private UUID destinationId;
  private String destinationFreeText;
  private String referenceNumber;
  private String invoiceNumber;
  private Double unitPrice;
  private List<StockEventAdjustmentDto> stockAdjustments;
  

  StockEventLineItem toEventLineItem() {
    // event is set in StockEventDto.toEvent()
    return new StockEventLineItem(
        orderableId, lotId, quantity, extraData, occurredDate, reasonId, reasonFreeText, sourceId,
        sourceFreeText, destinationId, destinationFreeText, null, 
        referenceNumber, invoiceNumber, unitPrice, stockAdjustments()
    );
  }

  public boolean hasReasonId() {
    return this.reasonId != null;
  }

  public boolean hasReasonFreeText() {
    return this.reasonFreeText != null;
  }

  public boolean hasLotId() {
    return this.lotId != null;
  }

  public boolean hasDestinationFreeText() {
    return this.destinationFreeText != null;
  }

  public boolean hasSourceFreeText() {
    return this.sourceFreeText != null;
  }

  public boolean hasSourceId() {
    return this.sourceId != null;
  }

  public boolean hasDestinationId() {
    return this.destinationId != null;
  }

  /**
   * Gets stock adjustments as {@link PhysicalInventoryLineItemAdjustment}.
   */
  public List<PhysicalInventoryLineItemAdjustment> stockAdjustments() {
    if (null == stockAdjustments) {
      return emptyList();
    }

    return stockAdjustments
        .stream()
        .map(StockEventAdjustmentDto::toPhysicalInventoryLineItemAdjustment)
        .collect(Collectors.toList());
  }
}
