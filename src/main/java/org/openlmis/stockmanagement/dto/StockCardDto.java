package org.openlmis.stockmanagement.dto;

import lombok.Builder;
import lombok.Data;
import org.openlmis.stockmanagement.domain.card.StockCard;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Builder
@Data
public class StockCardDto {
  private Integer stockOnHand;
  private FacilityDto facility;
  private ProgramDto program;
  private OrderableDto orderable;
  private List<StockCardLineItemDto> lineItems;

  /**
   * Create stock card dto from stock card.
   *
   * @param stockCard    stock card.
   * @param facilityDto  facility dto.
   * @param programDto   program dto.
   * @param orderableDto orderable dto.
   * @return the created stock card dto.
   */
  public static StockCardDto createFrom(StockCard stockCard, FacilityDto facilityDto,
                                        ProgramDto programDto, OrderableDto orderableDto) {
    List<StockCardLineItemDto> lineItemDtos = stockCard.getLineItems().stream()
            .map(StockCardLineItemDto::createFrom).collect(toList());

    return StockCardDto.builder()
            .lineItems(lineItemDtos)
            .facility(facilityDto)
            .program(programDto)
            .orderable(orderableDto)
            .build();
  }

  /**
   * Reorder line items by occurred date then by noticed date.
   */
  public void reorderLineItemsByDates() {
    Comparator<StockCardLineItemDto> byOccurred =
            comparing(item -> item.getLineItem().getOccurredDate());
    Comparator<StockCardLineItemDto> byNoticed =
            comparing(item -> item.getLineItem().getNoticedDate());

    setLineItems(lineItems.stream().sorted(byOccurred.thenComparing(byNoticed)).collect(toList()));
  }
}
