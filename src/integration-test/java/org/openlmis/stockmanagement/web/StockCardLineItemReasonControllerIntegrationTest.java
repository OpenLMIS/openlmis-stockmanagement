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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TAGS_INVALID;

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
import com.google.common.collect.Lists;
import javax.persistence.PersistenceException;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.openlmis.stockmanagement.i18n.MessageService;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaSystemException;

public class StockCardLineItemReasonControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/stockCardLineItemReasons";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";

  @Autowired
  private MessageService messageService;

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
  public void shouldReturn400WhenReasonTagIsTooLong() throws Exception {
    StockCardLineItemReason createdReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .withTags(Lists.newArrayList(RandomStringUtils.random(256)))
        .build();

    // specific psql format
    // C<<numbers>> -> sql state
    // M<<string>> -> short error message
    // \u0000 -> use for splitting parts of message
    // there are more fields but we need only those two for tests
    PSQLException psqlException = new PSQLException(
        new ServerErrorMessage(
            "C22001\u0000MERROR: Invalid stock card line item reason tag length"
        )
    );

    doThrow(new JpaSystemException((RuntimeException) new PersistenceException(psqlException)))
        .when(stockCardLineItemReasonService)
        .saveOrUpdate(any(StockCardLineItemReason.class));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(createdReason)
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body("message", Matchers.is(getMessage(ERROR_LINE_ITEM_REASON_TAGS_INVALID)));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
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

  private String getMessage(String messageKey, Object... messageParams) {
    return messageService.localize(new Message(messageKey, messageParams)).asMessage();
  }
}
