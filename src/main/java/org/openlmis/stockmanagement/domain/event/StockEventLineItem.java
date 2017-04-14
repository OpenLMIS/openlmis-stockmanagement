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

import org.openlmis.stockmanagement.domain.BaseEntity;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "stock_event_line_items", schema = "stockmanagement")
public class StockEventLineItem extends BaseEntity {

  private UUID lotId;
  private UUID orderableId;

  private Integer quantity;

  private ZonedDateTime occurredDate;

  private UUID reasonId;
  private String reasonFreeText;


  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent stockEvent;

  public boolean hasReason() {
    return this.reasonId != null;
  }

  public boolean hasReasonFreeText() {
    return this.reasonFreeText != null;
  }

  public boolean hasLot() {
    return this.lotId != null;
  }
}
