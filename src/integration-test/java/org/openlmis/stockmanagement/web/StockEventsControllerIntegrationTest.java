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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_ASSIGNMENT_NOT_FOUND;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.HomeFacilityPermissionService;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.service.StockEventValidationsService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;

public class StockEventsControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/stockEvents";

  @MockBean
  private StockEventValidationsService stockEventValidator;

  @MockBean
  private StockEventProcessor stockEventProcessor;

  @MockBean
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @Before
  public void setUp() {
    mockUserAuthenticated();
  }

  // POST /api/stockEvents

  @Test
  public void shouldCreateStockEvent() {
    // given
    mockHasPermissions();
    mockPassValidation();

    StockEventDto stockEvent = generateStockEvent();
    UUID expectedId = UUID.randomUUID();

    when(stockEventProcessor.process(any(StockEventDto.class)))
        .thenReturn(expectedId);

    // when
    UUID result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .body(stockEvent)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(UUID.class);

    // then
    assertEquals(expectedId, result);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestOnCreateStockEventWhenEntityInvalid() {
    // given
    mockHasPermissions();
    mockPassValidation();

    StockEventDto stockEvent = generateStockEvent();
    when(stockEventProcessor.process(any(StockEventDto.class)))
        .thenThrow(new ValidationMessageException(ERROR_REASON_ASSIGNMENT_NOT_FOUND));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON)
        .body(stockEvent)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private StockEventDto generateStockEvent() {
    StockEventDto stockEvent = new StockEventDto();

    stockEvent.setFacilityId(UUID.randomUUID());
    stockEvent.setProgramId(UUID.randomUUID());
    stockEvent.setSignature("signature");
    stockEvent.setDocumentNumber("number");
    stockEvent.setLineItems(Collections.singletonList(generateStockEventLineItem()));

    return stockEvent;
  }

  private StockEventLineItemDto generateStockEventLineItem() {
    StockEventLineItemDto item = new StockEventLineItemDto();

    item.setOrderableId(UUID.randomUUID());
    item.setLotId(UUID.randomUUID());
    item.setSourceId(UUID.randomUUID());
    item.setSourceFreeText("source");
    item.setDestinationId(UUID.randomUUID());
    item.setDestinationFreeText("destination");
    item.setQuantity(10);
    item.setReasonId(UUID.randomUUID());
    item.setReasonFreeText("reason");
    item.setOccurredDate(LocalDate.now());
    item.setExtraData(new HashMap<>());
    item.setStockAdjustments(new ArrayList<>());

    return item;
  }

  private void mockHasPermissions() {
    doNothing().when(homeFacilityPermissionService).checkProgramSupported(anyUuid());
    doNothing().when(permissionService).canAdjustStock(anyUuid(), anyUuid());
    doNothing().when(permissionService).canEditPhysicalInventory(anyUuid(), anyUuid());
  }

  private void mockPassValidation() {
    doNothing().when(stockEventValidator).validate(any(StockEventDto.class));
  }
}
