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
