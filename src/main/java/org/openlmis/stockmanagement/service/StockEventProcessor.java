package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.springframework.stereotype.Service;

/**
 * A service that is in charge of saving stock events and generating stock cards
 * and line items from stock events.
 */
@Service
public class StockEventProcessor {
  public StockEvent process(StockEvent event) {
    return null;
  }
}
