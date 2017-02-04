package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.dto.StockEventDto;

import java.util.UUID;

public class StockEventBuilder {
  /**
   * Create stock event dto object for test.
   *
   * @param uuid uuid.
   * @return the created event dto object.
   */
  public static StockEventDto createStockEventWithId(UUID uuid) {
    StockEventDto event = new StockEventDto();
    event.setId(uuid);
    return event;
  }
}
