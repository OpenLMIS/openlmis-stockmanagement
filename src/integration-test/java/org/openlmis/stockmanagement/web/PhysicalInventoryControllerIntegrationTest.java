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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.DispensableDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class PhysicalInventoryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/physicalInventories";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String PROGRAM_PARAM = "program";
  private static final String FACILITY_PARAM = "facility";

  @MockBean
  private PhysicalInventoryService physicalInventoryService;

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

  // GET /api/physicalInventories

  @Test
  public void shouldReturnNoContentWhenInventoriesDoNotExistForProgramAndFacility() {
    // given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(physicalInventoryService.findPhysicalInventory(programId, facilityId, null))
        .thenReturn(null);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_PARAM, programId)
        .queryParam(FACILITY_PARAM, facilityId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(204);

    // then
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
  public void shouldReturnBadRequestOnCreateEmptyDraftWhenEntityInvalid()
      throws InstantiationException, IllegalAccessException {
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
  public void shouldSaveDraftWhenValid() throws InstantiationException, IllegalAccessException {
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
  public void shouldReturnBadRequestOnSaveDraftWhenEntityInvalid()
      throws InstantiationException, IllegalAccessException {
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

  // DELETE /api/physicalInventories/draft

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
    item.setOrderable(generateOrderable());
    item.setStockAdjustments(new ArrayList<>());
    item.setLot(generateLot());
    item.setExtraData(new HashMap<>());

    return item;
  }

  private OrderableDto generateOrderable() {
    OrderableDto orderable = new OrderableDto();

    orderable.setId(UUID.randomUUID());
    orderable.setProductCode("code");
    orderable.setFullProductName("name");
    orderable.setIdentifiers(new HashMap<>());
    orderable.setExtraData(new HashMap<>());

    DispensableDto dispensable = new DispensableDto();
    dispensable.setDispensingUnit("unit");

    orderable.setDispensable(dispensable);

    return orderable;
  }

  private LotDto generateLot() {
    LotDto lot = new LotDto();

    lot.setId(UUID.randomUUID());
    lot.setLotCode("code");
    lot.setActive(true);
    lot.setTradeItemId(UUID.randomUUID());
    lot.setExpirationDate(LocalDate.now());
    lot.setManufactureDate(LocalDate.now());

    return lot;
  }
}
