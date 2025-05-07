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

package org.openlmis.stockmanagement.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateDataBuilder.createTemplateDto;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.dto.StockCardFieldDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemFieldDto;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardTemplateServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private StockCardTemplateService stockCardTemplateService;

  @MockBean
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Test
  public void shouldUpdateExistingTemplate() {
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
  public void shouldGetDefaultStockCardTemplate() {
    //when
    StockCardTemplateDto template = stockCardTemplateService.getDefaultStockCardTemplate();

    //then:
    assertThat(template.getStockCardFields().size(), is(4));
    assertThat(template.getStockCardLineItemFields().size(), is(5));

    assertThat(template.getStockCardFields().get(0).getName(), is("packSize"));
    assertThat(template.getStockCardLineItemFields().get(0).getName(), is("documentNumber"));
  }

  @Test
  public void shouldReturnNullWhenNoTemplateFound() {

    //when: searching for non-existing template
    StockCardTemplateDto dto = stockCardTemplateService
        .findByProgramIdAndFacilityTypeId(UUID.randomUUID(), UUID.randomUUID());

    //then
    assertNull(dto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotSaveTemplateWithUnavailableField() {
    //given
    StockCardTemplateDto templateDto = createTemplateDto();
    templateDto.getStockCardFields().add(new StockCardFieldDto("i do not exist", false, 1));

    //when
    stockCardTemplateService.saveOrUpdate(templateDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotSaveTemplateWithNonExistingProgramAndFacilityType() {
    //given: program and facility can not be found in ref data service
    doThrow(new ValidationMessageException("errorKey")).when(programFacilityTypeExistenceService)
        .checkProgramAndFacilityTypeExist(any(UUID.class), any(UUID.class));

    StockCardTemplateDto templateDto = createTemplateDto();

    //when
    stockCardTemplateService.saveOrUpdate(templateDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationExceptionWhenProgramIdMissing() throws Exception {
    StockCardTemplateDto templateDto = createTemplateDto();
    templateDto.setProgramId(null);

    //when
    stockCardTemplateService.saveOrUpdate(templateDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationExceptionWhenFacilityTypeIdMissing() throws Exception {
    StockCardTemplateDto templateDto = createTemplateDto();
    templateDto.setFacilityTypeId(null);

    //when
    stockCardTemplateService.saveOrUpdate(templateDto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenCardFieldsDuplicated() throws Exception {
    StockCardTemplateDto dto = createTemplateDto();
    dto.getStockCardFields().add(new StockCardFieldDto("packSize", true, 124));

    stockCardTemplateService.saveOrUpdate(dto);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenLineItemFieldsDuplicated() throws Exception {
    StockCardTemplateDto dto = createTemplateDto();
    dto.getStockCardLineItemFields().add(new StockCardLineItemFieldDto("documentNo", true, 457));

    stockCardTemplateService.saveOrUpdate(dto);
  }
}
