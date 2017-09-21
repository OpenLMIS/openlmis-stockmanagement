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

package org.openlmis.stockmanagement.domain.physicalinventory;

import static javax.persistence.CascadeType.ALL;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.event.StockEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "physical_inventories", schema = "stockmanagement")
public class PhysicalInventory extends BaseEntity {
  @Column(nullable = false)
  private UUID programId;

  @Column(nullable = false)
  private UUID facilityId;

  @Column(nullable = false)
  private Boolean isDraft;

  private LocalDate occurredDate;

  private String signature;
  private String documentNumber;

  @LazyCollection(FALSE)
  @OneToMany(cascade = ALL, mappedBy = "physicalInventory", orphanRemoval = true)
  private List<PhysicalInventoryLineItem> lineItems;

  @OneToOne
  private StockEvent stockEvent;
}
