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

package org.openlmis.stockmanagement.testutils;

import static java.util.Collections.singletonList;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StockEventDtoBuilder {

  /**
   * Create stock event dto object for testing.
   *
   * @return created dto object.
   */
  public static StockEventDto createStockEventDto() {
    StockEventDto stockEventDto = new StockEventDto();

    stockEventDto.setDocumentNumber("c");
    stockEventDto.setSignature("e");

    stockEventDto.setProgramId(UUID.randomUUID());
    stockEventDto.setFacilityId(UUID.randomUUID());

    StockEventLineItem eventLineItemDto = new StockEventLineItem();
    eventLineItemDto.setReasonFreeText("d");
    eventLineItemDto.setReasonId(UUID.fromString("e3fc3cf3-da18-44b0-a220-77c985202e06"));
    eventLineItemDto.setQuantity(1);
    eventLineItemDto.setOrderableId(UUID.randomUUID());
    eventLineItemDto.setOccurredDate(ZonedDateTime.now());
    eventLineItemDto.setSourceId(UUID.fromString("0bd28568-43f1-4836-934d-ec5fb11398e8"));
    eventLineItemDto.setDestinationId(UUID.fromString("087e81f6-a74d-4bba-9d01-16e0d64e9609"));
    eventLineItemDto.setSourceFreeText("a");
    eventLineItemDto.setDestinationFreeText("b");

    stockEventDto.setLineItems(singletonList(eventLineItemDto));
    return stockEventDto;
  }

  /**
   * Create stock event dto object without source and destination for testing.
   *
   * @return created dto object.
   */
  public static StockEventDto createNoSourceDestinationStockEventDto() {
    StockEventDto stockEventDto = createStockEventDto();
    StockEventLineItem stockEventLineItem = stockEventDto.getLineItems().get(0);
    stockEventLineItem.setSourceId(null);
    stockEventLineItem.setDestinationId(null);
    stockEventLineItem.setSourceFreeText(null);
    stockEventLineItem.setDestinationFreeText(null);
    return stockEventDto;
  }
}
