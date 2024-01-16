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

package org.openlmis.stockmanagement.domain.qualitychecks;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;

 
 
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_event_line_item_discrepencies", schema = "stockmanagement")
public class Discrepancy extends BaseEntity {
 
   private UUID rejectionReasonId;
   private Integer quantityAffected;
   private String comments;
 
   // Many-to-one relationship with StockEventLineItem
   @ManyToOne
   @JoinColumn(name = "stock_event_line_item_id")
   private StockEventLineItem stockEventLineItem;
 
   /**
    * Constructor for Discrepency.
    *
    */
   public Discrepancy(UUID rejectionReasonId, 
       Integer quantityAffected, String comments) {
     this.rejectionReasonId = rejectionReasonId;
     this.quantityAffected = quantityAffected;
     this.comments = comments;
     // stockEventLineItem can be set later or remain null
   }
}
 
 