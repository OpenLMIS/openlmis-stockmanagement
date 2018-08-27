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

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_card_line_item_reasons", schema = "stockmanagement")
public class StockCardLineItemReason extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String name;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String description;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(value = EnumType.STRING)
  private ReasonType reasonType;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(value = EnumType.STRING)
  private ReasonCategory reasonCategory;

  @Column(nullable = false)
  private Boolean isFreeTextAllowed = false;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
  @CollectionTable(
      name = "stock_card_line_item_reason_tags",
      joinColumns = @JoinColumn(name = "reasonId"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();

  /**
   * Creates new instance from importer.
   */
  public static StockCardLineItemReason newInstance(Importer importer) {
    StockCardLineItemReason reason = new StockCardLineItemReason(
        importer.getName(), importer.getDescription(), importer.getReasonType(),
        importer.getReasonCategory(), importer.getIsFreeTextAllowed(),
        importer.getTags()
    );
    reason.setId(importer.getId());

    return reason;
  }

  /**
   * Create physical credit reason.
   *
   * @return physical credit reason.
   */
  public static StockCardLineItemReason physicalCredit() {
    return builder()
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
        .name("Overstock(will be replaced by messages_lang.properties)")
        .tags(new ArrayList<>())
        .build();
  }

  /**
   * Create physical debit reason.
   *
   * @return physical debit reason.
   */
  public static StockCardLineItemReason physicalDebit() {
    return builder()
        .reasonType(ReasonType.DEBIT)
        .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
        .name("Understock(will be replaced by messages_lang.properties)")
        .tags(new ArrayList<>())
        .build();
  }

  /**
   * Create physical balance reason.
   *
   * @return physical balance reason.
   */
  public static StockCardLineItemReason physicalBalance() {
    return builder()
        .reasonType(ReasonType.BALANCE_ADJUSTMENT)
        .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
        .name("Balance adjustment(will be replaced by messages_lang.properties)")
        .tags(new ArrayList<>())
        .build();
  }

  public boolean isCreditReasonType() {
    return getReasonType() == ReasonType.CREDIT;
  }

  public boolean isDebitReasonType() {
    return getReasonType() == ReasonType.DEBIT;
  }

  public boolean isAdjustmentReasonCategory() {
    return getReasonCategory() == ReasonCategory.ADJUSTMENT;
  }

  public boolean hasNoName() {
    return Strings.isNullOrEmpty(name);
  }

  public boolean hasNoType() {
    return reasonType == null;
  }

  public boolean hasNoCategory() {
    return reasonCategory == null;
  }

  public boolean hasNoIsFreeTextAllowed() {
    return isFreeTextAllowed == null;
  }

  /**
   * Exports data into exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setReasonType(reasonType);
    exporter.setReasonCategory(reasonCategory);
    exporter.setIsFreeTextAllowed(isFreeTextAllowed);
    exporter.setTags(tags);
  }

  public interface Exporter {

    void setId(UUID id);

    void setName(String name);

    void setDescription(String description);

    void setReasonType(ReasonType reasonType);

    void setReasonCategory(ReasonCategory reasonCategory);

    void setIsFreeTextAllowed(Boolean isFreeTextAllowed);

    void setTags(List<String> tags);
  }

  public interface Importer {

    UUID getId();

    String getName();

    String getDescription();

    ReasonType getReasonType();

    ReasonCategory getReasonCategory();

    Boolean getIsFreeTextAllowed();

    List<String> getTags();

  }
}
