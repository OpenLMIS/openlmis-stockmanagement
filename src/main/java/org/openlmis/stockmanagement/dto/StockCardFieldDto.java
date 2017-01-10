package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockCardFieldDto {
  private String name;
  private boolean isDisplayed;
  private Integer displayOrder;
}
