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
import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.StockCardFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.utils.Message;

import java.util.List;
import java.util.Optional;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_CARD_FIELD_INVALID;

@Data
@AllArgsConstructor
public class StockCardFieldDto {
  private String name;
  private boolean isDisplayed;
  private Integer displayOrder;

  /**
   * Create stock card field dto object from DB model object.
   *
   * @param model the original object to convert from.
   * @return dto object.
   */
  static StockCardFieldDto from(StockCardFields model) {
    return new StockCardFieldDto(
            model.getAvailableStockCardFields().getName(),
            model.getIsDisplayed(),
            model.getDisplayOrder());
  }

  /**
   * Convert to DB model object.
   *
   * @param template   the template that this filed belongs to.
   * @param cardFields all available fields.
   * @return DB model object.
   */
  StockCardFields toModel(StockCardTemplate template,
                          List<AvailableStockCardFields> cardFields) {
    StockCardFields stockCardFields = new StockCardFields();
    stockCardFields.setStockCardTemplate(template);
    stockCardFields.setIsDisplayed(isDisplayed);
    stockCardFields.setDisplayOrder(displayOrder);
    stockCardFields.setAvailableStockCardFields(matchByName(cardFields));
    return stockCardFields;
  }

  private AvailableStockCardFields matchByName(List<AvailableStockCardFields> cardFields) {
    Optional<AvailableStockCardFields> first = cardFields.stream()
            .filter(field -> field.getName().equals(name))
            .findFirst();
    if (first.isPresent()) {
      return first.get();
    } else {
      throw new ValidationMessageException(
              new Message(ERROR_STOCK_CARD_FIELD_INVALID, name));
    }
  }
}
