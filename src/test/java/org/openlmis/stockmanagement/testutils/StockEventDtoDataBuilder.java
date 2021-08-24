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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.RandomStringUtils;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

public class StockEventDtoDataBuilder {

  private UUID resourceId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private String signature = RandomStringUtils.random(5);
  private String documentNumber = RandomStringUtils.random(5);
  private UUID userId = UUID.randomUUID();
  private Boolean isShowed = true;
  private List<StockEventLineItemDto> lineItems = new ArrayList<>();
  private StockEventProcessContext context;

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

    StockEventLineItemDto lineItem1 = new StockEventLineItemDto();
    lineItem1.setOrderableId(orderable);
    lineItem1.setLotId(lot);
    lineItem1.setQuantity(20);
    lineItem1.setOccurredDate(getBaseDate());
    lineItem1.setReasonId(UUID.fromString("279d55bd-42e3-438c-a63d-9c021b185dae"));

    StockEventLineItemDto lineItem2 = new StockEventLineItemDto();
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
   *createStockEventDto
   * @return created dto object.
   */
  public static StockEventDto createStockEventDto() {
    StockEventDto stockEventDto = new StockEventDto();

    stockEventDto.setDocumentNumber("c");
    stockEventDto.setSignature("e");

    stockEventDto.setProgramId(UUID.randomUUID());
    stockEventDto.setFacilityId(UUID.randomUUID());
    stockEventDto.setShowed(true);

    StockEventLineItemDto eventLineItemDto = createStockEventLineItem();

    stockEventDto.setLineItems(singletonList(eventLineItemDto));
    return stockEventDto;
  }

  /**
   * Create stock event line item dto object for testing.
   *
   * @return created dto object.
   */
  public static StockEventLineItemDto createStockEventLineItem() {
    StockEventLineItemDto eventLineItemDto = new StockEventLineItemDto();
    eventLineItemDto.setReasonFreeText("d");
    eventLineItemDto.setReasonId(UUID.fromString("e3fc3cf3-da18-44b0-a220-77c985202e06"));
    eventLineItemDto.setQuantity(1);
    eventLineItemDto.setOrderableId(UUID.randomUUID());
    eventLineItemDto.setOccurredDate(LocalDate.now());
    eventLineItemDto.setSourceId(UUID.fromString("cefcde83-7ee0-4a5a-9580-f32e3eec10ed"));
    eventLineItemDto.setDestinationId(UUID.fromString("0bd28568-43f1-4836-934d-ec5fb11398e8"));
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
    StockEventLineItemDto stockEventLineItem = stockEventDto.getLineItems().get(0);
    stockEventLineItem.setSourceId(null);
    stockEventLineItem.setDestinationId(null);
    stockEventLineItem.setSourceFreeText(null);
    stockEventLineItem.setDestinationFreeText(null);
    return stockEventDto;
  }

  /**
   * Create stock event dto object with source and destination for testing.
   *
   * @return created dto object.
   */
  public static StockEventDto createWithSourceAndDestination(UUID sourceId,
      UUID destinationId) {
    StockEventDto stockEventDto = createStockEventDto();
    StockEventLineItemDto stockEventLineItem = stockEventDto.getLineItems().get(0);

    stockEventLineItem.setSourceId(sourceId);
    stockEventLineItem.setDestinationId(destinationId);
    return stockEventDto;
  }

  public StockEventDto build() {
    return new StockEventDto(resourceId, facilityId, programId, signature, documentNumber, userId,
        isShowed, lineItems, context);
  }

  public StockEventDtoDataBuilder addLineItem(StockEventLineItemDto... lineItemDtos) {
    this.lineItems.addAll(Arrays.stream(lineItemDtos).collect(Collectors.toSet()));
    return this;
  }
}
