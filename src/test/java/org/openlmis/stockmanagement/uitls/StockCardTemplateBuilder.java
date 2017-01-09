package org.openlmis.stockmanagement.uitls;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;

import java.util.UUID;

public class StockCardTemplateBuilder {

  public static StockCardTemplate createTemplate() {
    StockCardTemplate template = new StockCardTemplate();
    template.setFacilityTypeId(UUID.randomUUID());
    template.setProgramId(UUID.randomUUID());
    template.getStockCardOptionalFields().setDonor(true);
    return template;
  }
}
