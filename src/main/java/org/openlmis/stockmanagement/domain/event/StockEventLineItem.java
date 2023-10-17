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

package org.openlmis.stockmanagement.domain.event;
//import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;

import static javax.persistence.CascadeType.ALL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.ExtraDataConverter;
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_event_line_items", schema = "stockmanagement")
public class StockEventLineItem extends BaseEntity
    implements IdentifiableByOrderableLot, VvmApplicable {

  @Column(nullable = false)
  private UUID orderableId;

  private UUID lotId;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  private Map<String, String> extraData;

  @Column(nullable = false)
  private LocalDate occurredDate;

  private UUID reasonId;
  private String reasonFreeText;

  private UUID sourceId;
  private String sourceFreeText;

  private UUID destinationId;
  private String destinationFreeText;

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent stockEvent;

  @OneToMany(
      cascade = ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @JoinColumn(name = "stockEventLineItemId")
  private List<PhysicalInventoryLineItemAdjustment> stockAdjustments;

/* 
  private String referenceNumber;
  private String invoiceNumber;
  private Double unitPrice;


  public StockEventLineItem(UUID orderableId, UUID lotId, Integer quantity, Map<String, String> extraData, LocalDate occurredDate, 
  UUID reasonId, String reasonFreeText, UUID sourceId, String sourceFreeText, UUID destinationId, String destinationFreeText, 
  List<StockEventAdjustmentDto> stockAdjustments, String referenceNumber, String invoiceNumber, Double unitPrice){

    orderableId=orderableId;
    lotId=lotId;
    quantity=quantity;
    extraData=extraData;
    occurredDate=occurredDate;
    reasonId=reasonId;
    reasonFreeText=reasonFreeText;
    sourceId= sourceId;
    sourceFreeText=sourceFreeText;
    destinationId=destinationId; 
    destinationFreeText=destinationFreeText; 
  
    stockAdjustments=stockAdjustments;
    referenceNumber=referenceNumber; 
    invoiceNumber=invoiceNumber;
    unitPrice=unitPrice;

  }
  /**
   * Returns clean copy of stock adjustments.
   */
  public List<PhysicalInventoryLineItemAdjustment> stockAdjustments() {
    if (getStockAdjustments() != null) {
      List<PhysicalInventoryLineItemAdjustment> newAdjustments =
              new ArrayList<>(stockAdjustments.size());
      getStockAdjustments().forEach(stockAdjustment ->
              newAdjustments.add(PhysicalInventoryLineItemAdjustment.builder()
          .reason(stockAdjustment.getReason())
          .quantity(stockAdjustment.getQuantity())
          .build()));
      return newAdjustments;
    }
    return null;
  }

  private String referenceNumber;

}
