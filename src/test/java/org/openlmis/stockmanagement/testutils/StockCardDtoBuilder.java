package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.domain.adjustment.ReasonCategory;
import org.openlmis.stockmanagement.domain.adjustment.ReasonType;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class StockCardDtoBuilder {
  /**
   * Create stock card dto.
   *
   * @return stock card dto
   */
  public static StockCardDto createStockCardDto() {
    StockCardLineItemReason reason = StockCardLineItemReason
            .builder()
            .name("Transfer In")
            .reasonCategory(ReasonCategory.ADJUSTMENT)
            .reasonType(ReasonType.CREDIT).build();


    StockCardLineItem lineItem = StockCardLineItem
            .builder()
            .quantity(1)
            .occurredDate(ZonedDateTime.of(2017, 2, 13, 4, 2, 18, 781000000, ZoneId.of("UTC")))
            .reason(reason).build();

    StockCardLineItemDto lineItemDto = StockCardLineItemDto
            .builder()
            .stockOnHand(1)
            .lineItem(lineItem)
            .source(FacilityDto.builder().name("HF1").build())
            .build();

    return StockCardDto.builder()
            .stockOnHand(1)
            .facility(FacilityDto.builder().name("HC01").build())
            .program(ProgramDto.builder().name("HIV").build())
            .orderable(OrderableDto.builder().productCode("ABC01").build())
            .lineItems(Arrays.asList(lineItemDto))
            .build();
  }
}
