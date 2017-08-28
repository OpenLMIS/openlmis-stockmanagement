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

import static javax.persistence.CascadeType.ALL;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.ExtraDataConverter;
import org.openlmis.stockmanagement.domain.common.VvmApplicable;
import org.openlmis.stockmanagement.domain.identity.IdentifiableByOrderableLot;
import org.openlmis.stockmanagement.domain.physicalinventory.StockAdjustment;

import lombok.Data;

import java.time.ZonedDateTime;
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
  private ZonedDateTime occurredDate;

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
  private List<StockAdjustment> stockAdjustments;

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

  public boolean isPhysicalInventory() {
    return sourceId == null && destinationId == null && reasonId == null;
  }

}
