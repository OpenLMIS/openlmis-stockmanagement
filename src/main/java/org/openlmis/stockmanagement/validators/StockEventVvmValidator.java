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

package org.openlmis.stockmanagement.validators;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_EVENT_ORDERABLE_DISABLED_VVM;

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.springframework.stereotype.Component;

/**
 * This validator ensures that stock event line items for orderables
 * with disabled VVM usage do not specify VVM Status.
 */
@Component("StockEventVvmValidator")
public class StockEventVvmValidator extends VvmValidator implements StockEventValidator {

  @Override
  public void validate(StockEventDto stockEventDto)
      throws InstantiationException, IllegalAccessException {
    validate(stockEventDto.getLineItems(), ERROR_STOCK_EVENT_ORDERABLE_DISABLED_VVM);
  }
}
