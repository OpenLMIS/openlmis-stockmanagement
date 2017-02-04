package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller used to create stock event.
 */
@Controller
@RequestMapping("/api")
public class StockEventsController {

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @RequestMapping(value = "stockEvents", method = POST)
  public ResponseEntity<StockEvent> createStockEvent(@RequestBody StockEvent stockEvent) {
    StockEvent createdEvent = stockEventProcessor.process(stockEvent);
    return new ResponseEntity<>(createdEvent, CREATED);
  }

}
