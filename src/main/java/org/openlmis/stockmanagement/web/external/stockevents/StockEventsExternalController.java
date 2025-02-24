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

package org.openlmis.stockmanagement.web.external.stockevents;

import java.util.UUID;
import org.openlmis.stockmanagement.web.BaseController;
import org.openlmis.stockmanagement.web.StockEventsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter Controller which converts {@link StockEventExternalDto} and delegates work to
 * {@link StockEventsExternalController}.
 */
@Transactional
@RestController
@RequestMapping("/api/public/stockEvents")
public class StockEventsExternalController extends BaseController {

  @Autowired
  private StockEventsAdapterBuilder builder;

  @Autowired
  private StockEventsController stockEventsController;

  @PostMapping
  public ResponseEntity<UUID> createStockEvent(@RequestBody StockEventExternalDto eventDto) {
    return stockEventsController.createStockEvent(builder.build(eventDto));
  }
}
