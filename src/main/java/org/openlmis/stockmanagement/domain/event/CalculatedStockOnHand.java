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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.card.StockCard;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "calculated_stocks_on_hand", schema = "stockmanagement")
public class CalculatedStockOnHand extends BaseEntity {

  @Column(nullable = false)
  private Integer stockOnHand;

  @ManyToOne
  @JoinColumn(name = "stockcardid", nullable = false)
  private StockCard stockCard;

  @Column(nullable = false)
  private LocalDate occurredDate;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime processedDate;
  
  /**
   * Exports data into exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setStockOnHand(getStockOnHand());
    exporter.setOccurredDate(getOccurredDate());
    exporter.setProcessedDate(getProcessedDate());
  }

  public interface Exporter {

    void setId(UUID id);

    void setStockOnHand(Integer stockOnHand);

    void setOccurredDate(LocalDate date);
    
    void setProcessedDate(ZonedDateTime processedDate);
  }
}
