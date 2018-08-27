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

package org.openlmis.stockmanagement.domain.reason;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.openlmis.stockmanagement.domain.BaseEntity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "valid_reason_assignments", schema = "stockmanagement")
public class ValidReasonAssignment extends BaseEntity {
  @Column(nullable = false)
  @Type(type = PG_UUID)
  private UUID programId;

  @Column(nullable = false)
  @Type(type = PG_UUID)
  private UUID facilityTypeId;

  @Column(nullable = false)
  private Boolean hidden;

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockCardLineItemReason reason;

  /**
   * Creates new instance based on data from {@link Importer}.
   *
   * @param importer instance of {@link Importer}
   * @return new instance of ValidReasonAssignment.
   */
  public static ValidReasonAssignment newInstance(Importer importer) {
    ValidReasonAssignment validReason = new ValidReasonAssignment();
    validReason.setId(importer.getId());
    validReason.setProgramId(importer.getProgramId());
    validReason.setFacilityTypeId(importer.getFacilityTypeId());
    validReason.setHidden(importer.getHidden() != null && importer.getHidden());
    validReason.setReason(importer.getReason());
    return validReason;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setProgramId(programId);
    exporter.setFacilityTypeId(facilityTypeId);
    exporter.setHidden(hidden);
    exporter.setReason(reason);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProgramId(UUID programId);

    void setFacilityTypeId(UUID facilityTypeId);

    void setHidden(Boolean hidden);

    void setReason(StockCardLineItemReason reason);
  }

  public interface Importer {
    UUID getId();

    UUID getProgramId();

    UUID getFacilityTypeId();

    Boolean getHidden();

    StockCardLineItemReason getReason();
  }
}
