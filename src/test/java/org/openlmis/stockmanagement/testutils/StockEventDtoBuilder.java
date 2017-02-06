package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.dto.StockEventDto;

public class StockEventDtoBuilder {
  /**
   * Create stock event dto object for test.
   *
   * @return the created event dto object.
   */
  public static StockEventDto createStockEventDto() {
    StockEventDto event = new StockEventDto();
    return event;
  }
}
