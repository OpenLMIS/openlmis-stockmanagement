package org.openlmis.stockmanagement.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockCardTemplateDto {

  private List<StockCardFieldDto> stockCardFields;
  private List<StockCardLineItemFieldDto> stockCardLineItemFields;
}
