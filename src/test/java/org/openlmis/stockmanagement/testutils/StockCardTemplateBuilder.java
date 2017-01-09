package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;

import java.util.UUID;

public class StockCardTemplateBuilder {

  /**
   * Create test object for stock card template.
   *
   * @return created object.
   */
  public static StockCardTemplate createTemplate() {
    StockCardTemplate template = new StockCardTemplate();
    template.setFacilityTypeId(UUID.randomUUID());
    template.setProgramId(UUID.randomUUID());
    template.getStockCardOptionalFields().setDonor(true);
    return template;
  }
}
