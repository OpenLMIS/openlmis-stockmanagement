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

package org.openlmis.stockmanagement.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;

@NoArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
public class StockCardLineItemReasonDataBuilder {
  private UUID id = UUID.randomUUID();
  private String name = "Donation";
  private String description = "Donation from the donor";
  private ReasonType reasonType = ReasonType.CREDIT;
  private ReasonCategory reasonCategory = ReasonCategory.TRANSFER;
  private Boolean isFreeTextAllowed = true;
  private List<String> tags = new ArrayList<>();

  public StockCardLineItemReasonDataBuilder withoutId() {
    return withId(null);
  }

  public StockCardLineItemReasonDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withName(String newName) {
    name = newName;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withDescription(String newDescription) {
    description = newDescription;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withCreditType() {
    return this.withReasonType(ReasonType.CREDIT);
  }

  public StockCardLineItemReasonDataBuilder withDebitType() {
    return this.withReasonType(ReasonType.DEBIT);
  }

  public StockCardLineItemReasonDataBuilder withReasonType(ReasonType type) {
    reasonType = type;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withoutCategory() {
    reasonCategory = null;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withPhysicalInventoryCategory() {
    reasonCategory = ReasonCategory.PHYSICAL_INVENTORY;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withAdjustmentCategory() {
    reasonCategory = ReasonCategory.ADJUSTMENT;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withTags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  public StockCardLineItemReasonDataBuilder withoutIsFreeTextAllowed() {
    isFreeTextAllowed = null;
    return this;
  }

  /**
   * Creates new instance of {@link StockCardLineItemReason} with properties.
   * @return created stock card line item reason.
   */
  public StockCardLineItemReason build() {
    StockCardLineItemReason reason = new StockCardLineItemReason(
        name, description, reasonType, reasonCategory, isFreeTextAllowed, tags
    );
    reason.setId(id);

    return reason;
  }

}
