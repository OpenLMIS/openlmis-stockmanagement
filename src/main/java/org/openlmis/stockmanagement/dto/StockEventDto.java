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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.movement.Node;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockEventDto {

  private Integer quantity;
  private UUID reasonId;

  private UUID facilityId;
  private UUID programId;
  private UUID orderableId;

  private UUID sourceId;
  private UUID destinationId;

  private ZonedDateTime occurredDate;
  private ZonedDateTime noticedDate;

  private String signature;

  private String reasonFreeText;
  private String sourceFreeText;
  private String destinationFreeText;

  private String documentNumber;

  /**
   * Convert dto to jpa model.
   *
   * @param userId user id.
   * @return the converted jpa model object.
   */
  public StockEvent toEvent(UUID userId)
          throws InstantiationException, IllegalAccessException {

    return new StockEvent(quantity, fromId(reasonId, StockCardLineItemReason.class),
            facilityId, programId, orderableId, userId,
            fromId(sourceId, Node.class), fromId(destinationId, Node.class),
            occurredDate, noticedDate, ZonedDateTime.now(),
            signature, reasonFreeText, sourceFreeText, destinationFreeText, documentNumber);
  }

  public boolean hasSource() {
    return this.sourceId != null;
  }

  public boolean hasDestination() {
    return this.destinationId != null;
  }
}
