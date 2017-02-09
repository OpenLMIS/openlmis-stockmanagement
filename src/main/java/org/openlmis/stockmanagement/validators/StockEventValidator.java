package org.openlmis.stockmanagement.validators;

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.springframework.stereotype.Component;

@Component
public interface StockEventValidator {
  void validate(StockEventDto stockEventDto);
}
