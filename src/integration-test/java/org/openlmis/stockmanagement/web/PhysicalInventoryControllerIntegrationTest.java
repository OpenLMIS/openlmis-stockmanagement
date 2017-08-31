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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.DispensableDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.HomeFacilityPermissionService;
import org.openlmis.stockmanagement.service.PhysicalInventoryService;
import org.springframework.boot.test.mock.mockito.MockBean;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class PhysicalInventoryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/physicalInventories";
  private static final String DRAFT_URL = RESOURCE_URL + "/draft";

  private static final String PROGRAM_PARAM = "program";
  private static final String FACILITY_PARAM = "facility";

  @MockBean
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @MockBean
  private PhysicalInventoryService physicalInventoryService;

  @Before
  public void setUp() {
    mockUserAuthenticated();
  }

  // GET /api/physicalInventories/draft

  @Test
  public void shouldGetChosenDraftWhenExists() {
    // given
    mockHasPermissions();

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    PhysicalInventoryDto expectedDraft = generatePhysicalInventory();

    when(physicalInventoryService.findDraft(eq(programId), eq(facilityId)))
        .thenReturn(expectedDraft);

    // when
    PhysicalInventoryDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_PARAM, programId)
        .queryParam(FACILITY_PARAM, facilityId)
        .when()
        .get(DRAFT_URL)
        .then()
        .statusCode(200)
        .extract().as(PhysicalInventoryDto.class);

    // then
    assertEquals(expectedDraft.getProgramId(), result.getProgramId());
    assertEquals(expectedDraft.getFacilityId(), result.getFacilityId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/physicalInventories/draft

  @Test
  public void shouldReturnNoContentWhenDraftDoesNotExist() {
    // given
    mockHasPermissions();

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(physicalInventoryService.findDraft(eq(programId), eq(facilityId)))
        .thenReturn(null);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_PARAM, programId)
        .queryParam(FACILITY_PARAM, facilityId)
        .when()
        .get(DRAFT_URL)
        .then()
        .statusCode(204);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /api/physicalInventories/draft

  @Test
  public void shouldSaveDraftWhenValid() throws InstantiationException, IllegalAccessException {
    // given
    mockHasPermissions();

    PhysicalInventoryDto expectedDraft = generatePhysicalInventory();
    when(physicalInventoryService.saveDraft(any(PhysicalInventoryDto.class)))
        .thenReturn(expectedDraft);

    // when
    PhysicalInventoryDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .body(expectedDraft)
        .when()
        .post(DRAFT_URL)
        .then()
        .statusCode(201)
        .extract().as(PhysicalInventoryDto.class);

    // then
    assertEquals(expectedDraft.getProgramId(), result.getProgramId());
    assertEquals(expectedDraft.getFacilityId(), result.getFacilityId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /api/physicalInventories/draft

  @Test
  public void shouldReturnBadRequestOnSaveDraftWhenEntityInvalid()
      throws InstantiationException, IllegalAccessException {
    // given
    mockHasPermissions();

    PhysicalInventoryDto expectedDraft = generatePhysicalInventory();
    when(physicalInventoryService.saveDraft(any(PhysicalInventoryDto.class)))
        .thenThrow(new ValidationMessageException(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .body(expectedDraft)
        .when()
        .post(DRAFT_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // DELETE /api/physicalInventories/draft

  @Test
  public void shouldDeleteDraftWhenExists() {
    // given
    mockHasPermissions();

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    doNothing().when(physicalInventoryService)
        .deleteExistingDraft(any(PhysicalInventoryDto.class));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .queryParam(PROGRAM_PARAM, programId)
        .queryParam(FACILITY_PARAM, facilityId)
        .when()
        .delete(DRAFT_URL)
        .then()
        .statusCode(204);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private PhysicalInventoryDto generatePhysicalInventory() {
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();

    inventory.setFacilityId(UUID.randomUUID());
    inventory.setProgramId(UUID.randomUUID());
    inventory.setOccurredDate(LocalDate.now());
    inventory.setDocumentNumber("number");
    inventory.setIsStarter(false);
    inventory.setSignature("signature");
    inventory.setLineItems(Collections.singletonList(generatePhysicalInventoryLineItem()));

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

  private void mockHasPermissions() {
    doNothing().when(homeFacilityPermissionService).checkProgramSupported(anyUuid());
    doNothing().when(permissionService).canAdjustStock(anyUuid(), anyUuid());
    doNothing().when(permissionService).canEditPhysicalInventory(anyUuid(), anyUuid());
  }
}
