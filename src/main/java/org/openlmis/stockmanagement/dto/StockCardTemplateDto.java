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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;

@Data
@JsonInclude(NON_NULL)
public class StockCardTemplateDto {

  private UUID programId;
  private UUID facilityTypeId;

  private List<StockCardFieldDto> stockCardFields = new ArrayList<>();
  private List<StockCardLineItemFieldDto> stockCardLineItemFields = new ArrayList<>();
  private Boolean isShowed;

  public void setShowed(Boolean showed) {
    isShowed = showed;
  }

  /**
   * Create stock card template dto object from DB model object.
   *
   * @param template the original object to convert from.
   * @return dto object.
   */
  public static StockCardTemplateDto from(StockCardTemplate template) {
    if (template == null) {
      return null;
    }

    StockCardTemplateDto dto = new StockCardTemplateDto();
    dto.setProgramId(template.getProgramId());
    dto.setFacilityTypeId(template.getFacilityTypeId());

    dto.stockCardFields = template.getStockCardFields().stream()
            .map(StockCardFieldDto::from).collect(toList());

    dto.stockCardLineItemFields = template.getStockCardLineItemFields().stream()
            .map(StockCardLineItemFieldDto::from).collect(toList());

    return dto;
  }

  /**
   * Convert to DB model object.
   *
   * @param availableCardFields     will be used to match stock card fields
   * @param availableLineItemFields will be use to match line item fields
   * @return DB model object.
   */
  public StockCardTemplate toModel(
          List<AvailableStockCardFields> availableCardFields,
          List<AvailableStockCardLineItemFields> availableLineItemFields) {

    StockCardTemplate template = new StockCardTemplate();
    template.setFacilityTypeId(this.getFacilityTypeId());
    template.setProgramId(this.getProgramId());
    template.setShowed(this.isShowed);

    template.setStockCardFields(stockCardFields.stream().distinct()
            .map(cardFieldDto -> cardFieldDto.toModel(template, availableCardFields))
            .collect(toList()));

    template.setStockCardLineItemFields(stockCardLineItemFields.stream()
            .map(lineItemFieldDto -> lineItemFieldDto.toModel(template, availableLineItemFields))
            .collect(toList()));

    return template;
  }
}
