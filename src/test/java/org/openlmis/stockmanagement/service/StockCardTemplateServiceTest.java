package org.openlmis.stockmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.uitls.StockCardTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardTemplateServiceTest {

  @Autowired
  private StockCardTemplateService stockCardTemplateService;

  @Test
  public void should_update_existing_template() throws Exception {
    //given: there is an existing template
    StockCardTemplate savedTemplate = stockCardTemplateService.saveOrUpdate(StockCardTemplateBuilder.createTemplate());

    //when: try to save a template with the same program and facility type
    savedTemplate.getStockCardOptionalFields().setPackSize(true);

    StockCardTemplate updatedTemplate = stockCardTemplateService.saveOrUpdate(savedTemplate);

    //then
    assertThat(updatedTemplate.getStockCardOptionalFields().getDonor(), is(true));
    assertThat(updatedTemplate.getStockCardOptionalFields().getPackSize(), is(true));
  }
}