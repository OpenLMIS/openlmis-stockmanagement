package org.openlmis.stockmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.dto.StockCardFieldDto;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateBuilder.createTemplateDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardTemplateServiceTest {

  @Autowired
  private StockCardTemplateService stockCardTemplateService;

  @Test
  public void should_update_existing_template() throws Exception {
    //given: there is an existing template
    StockCardTemplateDto savedTemplate = stockCardTemplateService.saveOrUpdate(createTemplateDto());
    UUID facilityTypeId = savedTemplate.getFacilityTypeId();
    UUID programId = savedTemplate.getProgramId();

    //when: try to save a template with the same program and facility type
    StockCardTemplateDto newTemplate = createTemplateDto();
    newTemplate.setFacilityTypeId(facilityTypeId);
    newTemplate.setProgramId(programId);
    newTemplate.getStockCardFields().get(0).setDisplayOrder(321);

    stockCardTemplateService.saveOrUpdate(newTemplate);
    StockCardTemplateDto updatedTemplate = stockCardTemplateService
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);

    //then
    assertThat(updatedTemplate.getStockCardFields().size(), is(1));

    StockCardFieldDto firstFields = updatedTemplate.getStockCardFields().get(0);
    assertThat(firstFields.getDisplayOrder(), is(321));
    assertThat(firstFields.getName(), is("packSize"));
  }

  @Test
  public void should_get_default_stock_card_template() throws Exception {
    //when
    StockCardTemplateDto template = stockCardTemplateService.getDefaultStockCardTemplate();

    //then:
    assertThat(template.getStockCardFields().size(), is(4));
    assertThat(template.getStockCardLineItemFields().size(), is(5));

    assertThat(template.getStockCardFields().get(0).getName(), is("packSize"));
    assertThat(template.getStockCardLineItemFields().get(0).getName(), is("documentNumber"));
  }
}