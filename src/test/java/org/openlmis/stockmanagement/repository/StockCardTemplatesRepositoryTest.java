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

package org.openlmis.stockmanagement.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateBuilder.createTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardTemplatesRepositoryTest {

  @Autowired
  private StockCardTemplatesRepository stockCardTemplatesRepository;

  @Test
  public void should_search_for_stock_card_template_by_facility_type_and_program()
          throws Exception {
    //given
    StockCardTemplate template = createTemplate();

    stockCardTemplatesRepository.save(template);

    //when
    StockCardTemplate found = stockCardTemplatesRepository.findByProgramIdAndFacilityTypeId(
            template.getProgramId(), template.getFacilityTypeId());

    //then
    AvailableStockCardFields packSize = found.getStockCardFields().get(0)
            .getAvailableStockCardFields();

    AvailableStockCardLineItemFields docNumber = found.getStockCardLineItemFields().get(0)
            .getAvailableStockCardLineItemFields();

    assertThat(packSize.getName(), is("packSize"));
    assertThat(docNumber.getName(), is("documentNumber"));
  }

}