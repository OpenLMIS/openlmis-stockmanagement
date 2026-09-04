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

package org.openlmis.stockmanagement.service.referencedata;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.DynamicPageTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

public class LotReferenceDataServiceTest extends BaseReferenceDataServiceTest<LotDto> {

  private LotReferenceDataService service;

  @Override
  protected BaseReferenceDataService<LotDto> getService() {
    return new LotReferenceDataService();
  }

  @Override
  protected LotDto generateInstance() {
    return new LotDto();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    service = (LotReferenceDataService) prepareService();
  }

  @Test
  public void getAllLotsOfTradeItemShouldReturnMatchingLots() {
    LotDto lot = mockPageResponseEntityAndGetDto();

    UUID tradeItemId = UUID.randomUUID();
    List<LotDto> response = service.getAllLotsOf(tradeItemId);

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(lot));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(LotDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?tradeItemId=" + tradeItemId.toString(),
        uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void getAllLotsExpiringOnDateShouldReturnMatchingLots() {
    LotDto lot = mockPageResponseEntityAndGetDto();

    LocalDate expirationDate = LocalDate.parse("2019-01-01");
    List<LotDto> response = service.getAllLotsExpiringOn(expirationDate);

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(lot));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(LotDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl()
            + "?expirationDate=" + expirationDate.toString(),
        uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void findByIdsShouldReturnMatchingLots() {
    LotDto lot = mockPageResponseEntityAndGetDto();

    List<LotDto> response = service.findByIds(singletonList(lot.getId()));

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(lot));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(LotDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?id=" + lot.getId().toString(),
        uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void findByExactCodesShouldReturnMatchingLots() {
    LotDto lot = mockPageResponseEntityAndGetDto();

    List<LotDto> response = service.findByExactCodes(singletonList(lot.getLotCode()));

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(lot));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(LotDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?exactCode=" + lot.getLotCode(), uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void createShouldPostLotAndReturnCreatedInstance() {
    LotDto created = LotDto.builder().id(UUID.randomUUID()).lotCode("NEW1").build();
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
        eq(LotDto.class))).thenReturn(new ResponseEntity<>(created, HttpStatus.CREATED));

    LotDto response = service.create(LotDto.builder().lotCode("NEW1").build());

    assertEquals(created, response);

    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.POST), entityCaptor.capture(),
        eq(LotDto.class));
    assertEquals(serviceUrl + service.getUrl(), uriCaptor.getValue().toString());
    assertAuthHeader(entityCaptor.getValue());
  }

  @Test
  public void createShouldTranslateClientErrorToValidationMessageException() {
    HttpStatusCodeException conflict = mock(HttpStatusCodeException.class);
    when(conflict.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
        eq(LotDto.class))).thenThrow(conflict);

    expectedException.expect(ValidationMessageException.class);

    service.create(LotDto.builder().lotCode("DUP1").build());
  }

  @Test
  public void createShouldTranslateServerErrorToDataRetrievalException() {
    HttpStatusCodeException serverError = mock(HttpStatusCodeException.class);
    when(serverError.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    when(serverError.getResponseBodyAsString()).thenReturn("boom");
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
        eq(LotDto.class))).thenThrow(serverError);

    expectedException.expect(DataRetrievalException.class);

    service.create(LotDto.builder().lotCode("ERR1").build());
  }
}
