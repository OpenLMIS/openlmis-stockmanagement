/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.stockmanagement.testutils;

import java.util.UUID;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardFields;
import org.openlmis.stockmanagement.domain.template.StockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.dto.StockCardFieldDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemFieldDto;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;

public class StockCardTemplateDataBuilder {

  /**
   * Create test object for stock card template.
   *
   * @return created object.
   */
  public static StockCardTemplate createTemplate() {
    StockCardTemplate template = new StockCardTemplate();
    template.setFacilityTypeId(UUID.randomUUID());
    template.setProgramId(UUID.randomUUID());

    AvailableStockCardFields packSize = new AvailableStockCardFields();
    packSize.setId(UUID.fromString("7663b4d2-d6da-11e6-bf26-cec0c932ce01"));

    AvailableStockCardLineItemFields docNumber = new AvailableStockCardLineItemFields();
    docNumber.setId(UUID.fromString("b15ad020-d6da-11e6-bf26-cec0c932ce01"));

    template.getStockCardFields()
            .add(new StockCardFields(template, packSize, true, 123));

    template.getStockCardLineItemFields()
            .add(new StockCardLineItemFields(template, docNumber, true, 456));

    return template;
  }

  /**
   * Create template dto.
   *
   * @return dto.
   */
  public static StockCardTemplateDto createTemplateDto() {
    StockCardTemplateDto dto = new StockCardTemplateDto();
    dto.setFacilityTypeId(UUID.randomUUID());
    dto.setProgramId(UUID.randomUUID());
    dto.setActive(true);
    dto.getStockCardFields().add(new StockCardFieldDto("packSize", true, 123));
    dto.getStockCardLineItemFields().add(
            new StockCardLineItemFieldDto("documentNumber", true, 456));
    return dto;
  }
}
