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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "physical_inventory_line_items", schema = "stockmanagement")
public class PhysicalInventoryLineItem
    extends BaseEntity
    implements VvmApplicable, IdentifiableByOrderableLot {
  @Column(nullable = false)
  private UUID orderableId;
  @Column
  private UUID lotId;
  @Column
  private UUID unitOfOrderableId;

  //when saving draft quantity is allowed to be null
  //do NOT annotate this field as nullable = false
  private Integer quantity;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  private Map<String, String> extraData;

  @ManyToOne()
  @JoinColumn(nullable = false)
  private PhysicalInventory physicalInventory;

  @OneToMany(
          cascade = ALL,
          fetch = FetchType.LAZY,
          orphanRemoval = true)
  @JoinColumn(name = "physicalInventoryLineItemId")
  private List<PhysicalInventoryLineItemAdjustment> stockAdjustments;

  private Integer previousStockOnHandWhenSubmitted;
}
