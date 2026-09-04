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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_TYPE_INVALID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public final class StockCardLineItemReasonDto
    extends BaseDto
    implements StockCardLineItemReason.Importer, StockCardLineItemReason.Exporter {

  private String name;

  private String description;

  @JsonProperty("reasonType")
  private String type;

  @JsonProperty("reasonCategory")
  private String category;

  private Boolean isFreeTextAllowed;

  private Set<String> tags;

  /**
   * Creates new instance based on data from the domain object.
   */
  public static StockCardLineItemReasonDto newInstance(StockCardLineItemReason domain) {
    StockCardLineItemReasonDto dto = new StockCardLineItemReasonDto();
    domain.export(dto);

    return dto;
  }

  @Override
  @JsonIgnore
  public ReasonType getReasonType() {
    ReasonType reasonType = ReasonType.fromString(type);

    if (null == reasonType) {
      throw new ValidationMessageException(new Message(ERROR_REASON_TYPE_INVALID, type));
    }

    return reasonType;
  }

  @Override
  @JsonIgnore
  public void setReasonType(ReasonType reasonType) {
    this.type = reasonType != null ? reasonType.toString() : null;
  }

  @Override
  @JsonIgnore
  public ReasonCategory getReasonCategory() {
    ReasonCategory reasonCategory = ReasonCategory.fromString(category);

    if (null == reasonCategory) {
      throw new ValidationMessageException(new Message(ERROR_REASON_CATEGORY_INVALID));
    }

    return reasonCategory;
  }


  @Override
  @JsonIgnore
  public void setReasonCategory(ReasonCategory reasonCategory) {
    this.category = reasonCategory != null ? reasonCategory.toString() : null;
  }

}
