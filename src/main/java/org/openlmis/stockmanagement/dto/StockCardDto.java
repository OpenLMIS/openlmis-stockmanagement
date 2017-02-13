package org.openlmis.stockmanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class StockCardDto {
  private Integer stockOnHand;
  private FacilityDto facility;
  private ProgramDto program;
  private OrderableDto orderable;
  private List<StockCardLineItemDto> lineItems;
}
