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
public class StockCardTemplateRepositoryTest {

  @Autowired
  private StockCardTemplateRepository stockCardTemplateRepository;

  @Test
  public void should_search_for_stock_card_template_by_facility_type_and_program()
          throws Exception {
    //given
    StockCardTemplate template = createTemplate();

    stockCardTemplateRepository.save(template);

    //when
    StockCardTemplate found = stockCardTemplateRepository.findByProgramIdAndFacilityTypeId(
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