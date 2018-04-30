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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;


public class StockCardLineItemReasonControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/stockCardLineItemReasons";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @Test
  public void shouldCreateReason() {
    //given
    StockCardLineItemReason createdReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .build();

    when(stockCardLineItemReasonService.saveOrUpdate(any(StockCardLineItemReason.class)))
        .thenReturn(createdReason);

    //when
    StockCardLineItemReasonDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .body(createdReason)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .as(StockCardLineItemReasonDto.class);

    //then
    assertThat(response, is(StockCardLineItemReasonDto.newInstance(createdReason)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnErrorIfUserHasNoPermissionToCreate() {
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageReasons();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .body(new StockCardLineItemReasonDto())
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @Test
  public void shouldUpdateReason() {
    //given
    StockCardLineItemReason updatedReason = new StockCardLineItemReasonDataBuilder()
        .withDescription("test reason")
        .build();

    when(stockCardLineItemReasonService.saveOrUpdate(any(StockCardLineItemReason.class)))
        .thenReturn(updatedReason);

    //when
    StockCardLineItemReasonDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .body(updatedReason)
        .pathParam(ID_FIELD, updatedReason.getId())
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(StockCardLineItemReasonDto.class);

    //then
    assertThat(response, is(StockCardLineItemReasonDto.newInstance(updatedReason)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnErrorIfUserHasNoPermissionToUpdate() {
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageReasons();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .body(new StockCardLineItemReasonDto())
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @Test
  public void shouldReturnAllReasons() {
    //given
    StockCardLineItemReason reason1 = new StockCardLineItemReasonDataBuilder().build();

    StockCardLineItemReason reason2 = new StockCardLineItemReasonDataBuilder()
        .withName("Another test reason")
        .build();

    when(stockCardLineItemReasonRepository.findAll())
        .thenReturn(Arrays.asList(reason1, reason2));

    //when
    List<StockCardLineItemReasonDto> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(List.class);

    //then
    assertThat(response, hasSize(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnReason() {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().build();

    when(stockCardLineItemReasonRepository.findOne(reason.getId()))
        .thenReturn(reason);

    //when
    StockCardLineItemReasonDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID_FIELD, reason.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(StockCardLineItemReasonDto.class);

    //then
    assertThat(response, is(StockCardLineItemReasonDto.newInstance(reason)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnErrorMessageIfReasonDoesNotExist() {
    //given
    when(stockCardLineItemReasonRepository.findOne(any(UUID.class))).thenReturn(null);

    //when
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID_FIELD, UUID.randomUUID())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_REASON_NOT_FOUND));

    //then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
