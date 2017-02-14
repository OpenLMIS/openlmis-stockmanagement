package org.openlmis.stockmanagement.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;

@Builder
@Data
public class StockCardLineItemDto {

  private Integer stockOnHand;

  @JsonUnwrapped
  private StockCardLineItem lineItem;

  private FacilityDto source;
  private FacilityDto destination;

  /**
   * Create stock card line item dto from stock card line item.
   *
   * @param stockCardLineItem stock card line item.
   * @return the created stock card line item dto.
   */
  public static StockCardLineItemDto createFrom(StockCardLineItem stockCardLineItem) {
    return StockCardLineItemDto.builder()
            .lineItem(stockCardLineItem)
            .build();
  }
}
