package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.domain.event.StockEvent;

public class StockEventBuilder {
  /**
   * Create stock event object for test.
   * @param quantity quantity.
   * @return the created event object.
   */
  public static StockEvent createStockEventWithQuantity(int quantity) {
    StockEvent event = new StockEvent();
    event.setQuantity(quantity);
    return event;
  }
}
