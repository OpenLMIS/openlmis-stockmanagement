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

package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.StockCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/api")
public class StockCardsController {

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockCardRepository stockCardRepository;

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

  /**
   * Get stock card ids.
   *
   * @return all stock card ids.
   */
  @RequestMapping(value = "/stockCardIds")
  public ResponseEntity<String> getStockCardIds() {
    String ids = String.join(",", stream(stockCardRepository.findAll().spliterator(), false)
            .map(card -> card.getId().toString()).collect(Collectors.toList()));
    String warning = "this is for showcase convenience, will be removed afterwards.";
    return new ResponseEntity<String>(warning + ids, OK);
  }

}
