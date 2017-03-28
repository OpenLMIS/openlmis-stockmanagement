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

package org.openlmis.stockmanagement.service;


import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.validators.StockEventValidator2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockEventValidationsService2 {

  @Autowired
  private List<StockEventValidator2> stockEventValidators;

  /**
   * Validate stock event with permission service and all validators.
   *
   * @param stockEventDto the event to be validated.
   */
  public void validate(StockEventDto2 stockEventDto)
      throws InstantiationException, IllegalAccessException {
    for (StockEventValidator2 validator : stockEventValidators) {
      validator.validate(stockEventDto);
    }
  }

}
