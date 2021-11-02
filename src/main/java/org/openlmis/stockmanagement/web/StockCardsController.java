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

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardService;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StockCardsController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardsController.class);

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  /**
   * Get stock card by id.
   *
   * @param stockCardId stock card id.
   * @return found stock card.
   */
  @RequestMapping(value = "/stockCards/{stockCardId}")
  public ResponseEntity<StockCardDto> getStockCard(@PathVariable("stockCardId") UUID stockCardId) {
    LOGGER.debug("Try to find stock card with id: {}", stockCardId);

    StockCardDto stockCardDto = stockCardService.findStockCardById(stockCardId);
    if (stockCardDto == null) {
      LOGGER.debug("Not found stock card with id: {}", stockCardId);
      return new ResponseEntity<>(NOT_FOUND);
    } else {
      LOGGER.debug("Found stock card with id: {}", stockCardId);
      return new ResponseEntity<>(stockCardDto, OK);
    }
  }

  /**
   * Search Stock Cards by multiple ids.
   *
   * @param params   stock cards search parameters
   * @param pageable stock cards pagination parameters
   * @return found stock card.
   */
  @RequestMapping(value = "/stockCards")
  public Page<StockCardDto> search(@RequestParam MultiValueMap<String, String> params,
      Pageable pageable) {
    return stockCardService.search(UuidUtil.getIds(params), pageable);
  }

  /**
   * Get stock card summaries by program and facility.
   *
   * @return Stock card summaries.
   */
  @RequestMapping(value = "/stockCardSummaries")
  public Page<StockCardDto> getStockCardSummaries(
      @RequestParam() UUID program,
      @RequestParam() UUID facility,
      Pageable pageable
  ) {
    LOGGER.debug("Try to find stock card summaries");
    permissionService.canViewStockCard(program, facility);
    return stockCardSummariesService.findStockCards(program, facility, pageable);
  }

  /**
   * Get dummy stock card summaries for approved products and lots.
   *
   * @return Stock card summaries.
   */
  @RequestMapping(value = "/stockCardSummaries/noCards")
  public List<StockCardDto> getStockCardSummariesFor(
      @RequestParam() UUID program,
      @RequestParam() UUID facility
  ) {
    LOGGER.debug("dummy stock card summaries for approved orderables and lots");
    permissionService.canViewStockCard(program, facility);
    return stockCardSummariesService.createDummyStockCards(program, facility);
  }

  /**
   * Makes stock card inactive.
   *
   * @param stockCardId stock card id.
   */
  @PutMapping("/stockCards/{stockCardId}/deactivate")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<StockCardDto> deactivate(
      @PathVariable("stockCardId") UUID stockCardId) {
    LOGGER.debug("Try to make stock card with id: {} inactive", stockCardId);

   try {
     stockCardService.setInactive(stockCardId);
     LOGGER.debug("Stock card with id: {} made inactive", stockCardId);
      return new ResponseEntity<>(StockCardDto.createFrom(stockCard.get()), OK);
    } catch(ResourceNotFoundException e) {
      LOGGER.debug(e);
      return new ResponseEntity<>(NOT_FOUND);
    }
  }
}
