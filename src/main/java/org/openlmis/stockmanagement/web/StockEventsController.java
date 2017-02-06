package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

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
  public ResponseEntity<UUID> createStockEvent(@RequestBody StockEventDto stockEventDto) {
    UUID createdEventId = stockEventProcessor.process(stockEventDto);
    return new ResponseEntity<>(createdEventId, CREATED);
  }

}
