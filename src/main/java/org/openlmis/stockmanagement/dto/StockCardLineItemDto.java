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
}
