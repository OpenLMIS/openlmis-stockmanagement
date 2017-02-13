package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.service.StockCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/api")
public class StockCardsController {

  @Autowired
  private StockCardService stockCardService;

  /**
   * Get stock card by id.
   *
   * @param stockCardId stock card id.
   * @return found stock card.
   */
  @RequestMapping(value = "/stockCards/{stockCardId}")
  public ResponseEntity<StockCardDto> getStockCard(@PathVariable("stockCardId") UUID stockCardId) {
    StockCardDto stockCardDto = stockCardService.findStockCardById(stockCardId);
    if (stockCardDto == null) {
      return new ResponseEntity<>(NOT_FOUND);
    } else {
      return new ResponseEntity<>(stockCardDto, OK);
    }
  }
}
