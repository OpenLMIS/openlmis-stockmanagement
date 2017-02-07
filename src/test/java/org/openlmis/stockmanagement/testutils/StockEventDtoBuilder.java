package org.openlmis.stockmanagement.testutils;

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

    stockEventDto.setSourceFreeText("a");
    stockEventDto.setDestinationFreeText("b");
    stockEventDto.setDocumentNumber("c");
    stockEventDto.setReasonFreeText("d");
    stockEventDto.setSignature("e");

    stockEventDto.setQuantity(1);
    stockEventDto.setReasonId(UUID.randomUUID());

    stockEventDto.setSourceId(UUID.randomUUID());
    stockEventDto.setDestinationId(UUID.randomUUID());

    stockEventDto.setProgramId(UUID.randomUUID());
    stockEventDto.setFacilityId(UUID.randomUUID());
    stockEventDto.setOrderableId(UUID.randomUUID());

    stockEventDto.setStockCardId(UUID.randomUUID());

    stockEventDto.setNoticedDate(ZonedDateTime.now());
    stockEventDto.setOccurredDate(ZonedDateTime.now());
    return stockEventDto;
  }

}
