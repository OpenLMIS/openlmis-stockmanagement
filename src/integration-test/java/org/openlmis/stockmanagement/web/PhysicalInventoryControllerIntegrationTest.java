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

package org.openlmis.stockmanagement.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;
import static org.openlmis.stockmanagement.web.PhysicalInventoryController.PRINT_PI;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.JasperTemplateService;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;

@SuppressWarnings("PMD.TooManyMethods")
public class PhysicalInventoryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/physicalInventories";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String PROGRAM_PARAM = "program";
  private static final String FACILITY_PARAM = "facility";

  @MockBean
  private PhysicalInventoryService physicalInventoryService;

  @MockBean
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @MockBean
  private JasperTemplateService jasperTemplateService;

  @MockBean
  private JasperReportService jasperReportService;

  @Before
  public void setUp() {
    mockUserAuthenticated();
  }

  // GET /api/physicalInventories

  @Test
  public void shouldGetChosenDraftWhenExists() {
    // given
    PhysicalInventoryDto expectedDraft = generatePhysicalInventory();
    UUID programId = expectedDraft.getProgramId();
    UUID facilityId = expectedDraft.getFacilityId();

    when(physicalInventoryService.findPhysicalInventory(programId, facilityId, true))
        .thenReturn(Collections.singletonList(expectedDraft));

    // when
    List resultCollection = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_PARAM, programId)
        .queryParam(FACILITY_PARAM, facilityId)
        .queryParam("isDraft", true)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(List.class);

    // then
    assertEquals(1, resultCollection.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnEmptyListWhenInventoriesDoNotExistForProgramAndFacility() {
    // given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(physicalInventoryService.findPhysicalInventory(programId, facilityId, null))
        .thenReturn(Collections.emptyList());

    // when
    List response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_PARAM, programId)
        .queryParam(FACILITY_PARAM, facilityId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(List.class);

    // then
    assertTrue(response.isEmpty());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/physicalInventories/{id}

  @Test
  public void shouldReturnOnePhysicalInventory() {
    // given
    PhysicalInventoryDto expectedInventory = generatePhysicalInventory();

    UUID piId = UUID.randomUUID();
    when(physicalInventoriesRepository.findById(piId))
        .thenReturn(Optional.of(expectedInventory.toPhysicalInventoryForDraft()));

    // when
    PhysicalInventoryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", piId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(expectedInventory.getClass());

    // then
    assertEquals(expectedInventory.getId(), response.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /api/physicalInventories

  @Test
  public void shouldCreateEmptyDraftWhenValid() {
    // given
    PhysicalInventoryDto request = generateDraft();
    PhysicalInventoryDto expectedDraft = new PhysicalInventoryDto();
    expectedDraft.setProgramId(request.getProgramId());
    expectedDraft.setFacilityId(request.getFacilityId());
    expectedDraft.setId(UUID.randomUUID());

    when(physicalInventoryService
        .createNewDraft(request))
        .thenReturn(expectedDraft);

    // when
    PhysicalInventoryDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .body(request)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(PhysicalInventoryDto.class);

    // then
    assertEquals(expectedDraft, result);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestOnCreateEmptyDraftWhenEntityInvalid() {
    // given
    PhysicalInventoryDto expectedDraft = generateDraft();
    when(physicalInventoryService
        .createNewDraft(expectedDraft))
        .thenThrow(new ValidationMessageException(ERROR_PROGRAM_ID_MISSING));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .body(expectedDraft)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // PUT /api/physicalInventories/{id}

  @Test
  public void shouldSaveDraftWhenValid() {
    // given
    UUID physicalInventoryId = UUID.randomUUID();
    PhysicalInventoryDto expectedDraft = generatePhysicalInventory();
    when(physicalInventoryService
        .saveDraft(expectedDraft, physicalInventoryId))
        .thenReturn(expectedDraft);

    // when
    PhysicalInventoryDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", physicalInventoryId)
        .contentType(APPLICATION_JSON)
        .body(expectedDraft)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(PhysicalInventoryDto.class);

    // then
    assertEquals(expectedDraft, result);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestOnSaveDraftWhenEntityInvalid() {
    // given
    UUID physicalInventoryId = UUID.randomUUID();
    PhysicalInventoryDto expectedDraft = generatePhysicalInventory();
    when(physicalInventoryService
        .saveDraft(expectedDraft, physicalInventoryId))
        .thenThrow(new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .pathParam("id", physicalInventoryId)
        .body(expectedDraft)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // DELETE /api/physicalInventories

  @Test
  public void shouldReturnNoContentAfterDeleteDraft() {
    // given
    UUID physicalInventoryId = UUID.randomUUID();

    doNothing().when(physicalInventoryService)
        .deletePhysicalInventory(physicalInventoryId);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .pathParam("id", physicalInventoryId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenInventoryIsSubmitted() {
    // given
    UUID physicalInventoryId = UUID.randomUUID();

    doThrow(new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED))
        .when(physicalInventoryService)
        .deletePhysicalInventory(physicalInventoryId);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .pathParam("id", physicalInventoryId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenInventoryIsNotFound() {
    // given
    UUID physicalInventoryId = UUID.randomUUID();

    doThrow(new ResourceNotFoundException(ERROR_PHYSICAL_INVENTORY_NOT_FOUND))
        .when(physicalInventoryService)
        .deletePhysicalInventory(physicalInventoryId);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .pathParam("id", physicalInventoryId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPrintInventoryItem() {
    PhysicalInventory physicalInventory = generatePhysicalInventory()
        .toPhysicalInventoryForSubmit();
    JasperTemplate template = new JasperTemplate();
    template.setName("Test template");

    when(jasperTemplateService.getByName(PRINT_PI)).thenReturn(template);
    when(physicalInventoriesRepository.findById(physicalInventory.getId()))
        .thenReturn(Optional.of(physicalInventory));

    when(jasperReportService.generateReport(any(JasperTemplate.class), anyMap()))
        .thenReturn(new byte[1]);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", physicalInventory.getId())
        .queryParam("format", "pdf")
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200);
  }

  private PhysicalInventoryDto generatePhysicalInventory() {
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();

    inventory.setId(UUID.randomUUID());
    inventory.setFacilityId(UUID.randomUUID());
    inventory.setProgramId(UUID.randomUUID());
    inventory.setOccurredDate(LocalDate.now());
    inventory.setDocumentNumber("number");
    inventory.setIsStarter(false);
    inventory.setSignature("signature");
    inventory.setLineItems(Collections.singletonList(generatePhysicalInventoryLineItem()));

    return inventory;
  }

  private PhysicalInventoryDto generateDraft() {
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setFacilityId(UUID.randomUUID());
    inventory.setProgramId(UUID.randomUUID());
    return inventory;
  }

  private PhysicalInventoryLineItemDto generatePhysicalInventoryLineItem() {
    PhysicalInventoryLineItemDto item = new PhysicalInventoryLineItemDto();

    item.setQuantity(10);
    item.setStockOnHand(5);
    item.setOrderableId(UUID.randomUUID());
    item.setStockAdjustments(new ArrayList<>());
    item.setLotId(UUID.randomUUID());
    item.setExtraData(new HashMap<>());

    return item;
  }
}
