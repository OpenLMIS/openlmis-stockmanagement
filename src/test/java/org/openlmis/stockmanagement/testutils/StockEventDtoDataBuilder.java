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
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDate;

import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

public class StockEventDtoDataBuilder {

  /**
   * Create stock event dto object for testing with two line items.
   *
   * @return created dto object.
   */
  public static StockEventDto createStockEventDtoWithTwoLineItems() {
    StockEventDto stockEventDto = new StockEventDto();
    stockEventDto.setProgramId(UUID.randomUUID());
    stockEventDto.setFacilityId(UUID.randomUUID());

    UUID orderable = UUID.randomUUID();
    UUID lot = UUID.randomUUID();

    StockEventLineItem lineItem1 = new StockEventLineItem();
    lineItem1.setOrderableId(orderable);
    lineItem1.setLotId(lot);
    lineItem1.setQuantity(20);
    lineItem1.setOccurredDate(getBaseDate());
    lineItem1.setReasonId(UUID.fromString("279d55bd-42e3-438c-a63d-9c021b185dae"));

    StockEventLineItem lineItem2 = new StockEventLineItem();
    lineItem2.setOrderableId(orderable);
    lineItem2.setLotId(lot);
    lineItem2.setQuantity(10);
    lineItem2.setOccurredDate(getBaseDate());
    lineItem2.setReasonId(UUID.fromString("b7e99f5b-af04-433d-9c30-d4f90c60c47b"));

    stockEventDto.setLineItems(Arrays.asList(lineItem1, lineItem2));
    return stockEventDto;
  }

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

    StockEventLineItem eventLineItemDto = createStockEventLineItem();

    stockEventDto.setLineItems(singletonList(eventLineItemDto));
    return stockEventDto;
  }

  /**
   * Create stock event line item dto object for testing.
   *
   * @return created dto object.
   */
  public static StockEventLineItem createStockEventLineItem() {
    StockEventLineItem eventLineItemDto = new StockEventLineItem();
    eventLineItemDto.setReasonFreeText("d");
    eventLineItemDto.setReasonId(UUID.fromString("e3fc3cf3-da18-44b0-a220-77c985202e06"));
    eventLineItemDto.setQuantity(1);
    eventLineItemDto.setOrderableId(UUID.randomUUID());
    eventLineItemDto.setOccurredDate(LocalDate.now());
    eventLineItemDto.setSourceId(UUID.fromString("0bd28568-43f1-4836-934d-ec5fb11398e8"));
    eventLineItemDto.setDestinationId(UUID.fromString("087e81f6-a74d-4bba-9d01-16e0d64e9609"));
    eventLineItemDto.setSourceFreeText("a");
    eventLineItemDto.setDestinationFreeText("b");
    return eventLineItemDto;
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
