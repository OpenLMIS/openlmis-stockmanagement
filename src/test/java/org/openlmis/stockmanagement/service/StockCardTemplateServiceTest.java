package org.openlmis.stockmanagement.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardFieldDto;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.exception.FieldsNotAvailableException;
import org.openlmis.stockmanagement.service.referencedata.FacilityTypeReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateBuilder.createTemplateDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardTemplateServiceTest {

  @Autowired
  private StockCardTemplateService stockCardTemplateService;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @MockBean
  private FacilityTypeReferenceDataService facilityTypeReferenceDataService;

  @Before
  public void setUp() throws Exception {
    //pretend that program and facility type are both present in ref data service
    when(programReferenceDataService.findOne(Matchers.any()))
            .thenReturn(new ProgramDto());
    when(facilityTypeReferenceDataService.findOne(Matchers.any()))
            .thenReturn(new FacilityTypeDto());
  }

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

  @Test
  public void should_return_null_when_no_template_found() throws Exception {

    //when: searching for non-existing template
    StockCardTemplateDto dto = stockCardTemplateService
            .findByProgramIdAndFacilityTypeId(UUID.randomUUID(), UUID.randomUUID());

    //then
    assertNull(dto);
  }

  @Test(expected = FieldsNotAvailableException.class)
  public void should_not_save_template_with_unavailable_field() throws Exception {
    //given
    StockCardTemplateDto templateDto = createTemplateDto();
    templateDto.getStockCardFields().add(new StockCardFieldDto("i do not exist", false, 1));

    //when
    stockCardTemplateService.saveOrUpdate(templateDto);
  }

}