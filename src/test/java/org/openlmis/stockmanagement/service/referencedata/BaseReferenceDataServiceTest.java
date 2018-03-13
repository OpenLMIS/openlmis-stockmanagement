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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.service.BaseCommunicationService;
import org.openlmis.stockmanagement.service.BaseCommunicationServiceTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

public abstract class BaseReferenceDataServiceTest<T> extends BaseCommunicationServiceTest {

  @Test
  public void shouldFindById() throws Exception {
    // given
    BaseReferenceDataService<T> service = prepareService();
    UUID id = UUID.randomUUID();
    T instance = generateInstance();
    ResponseEntity<T> response = mock(ResponseEntity.class);

    // when
    when(response.getBody()).thenReturn(instance);
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(service.getResultClass())
    )).thenReturn(response);

    T found = service.findOne(id);

    // then
    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET),
            entityCaptor.capture(), eq(service.getResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + id;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(found, is(instance));

    assertAuthHeader(entityCaptor.getValue());
    assertThat(entityCaptor.getValue().getBody(), is(nullValue()));
  }

  @Test
  public void shouldReturnNullIfEntityCannotBeFoundById() throws Exception {
    // given
    BaseReferenceDataService<T> service = prepareService();
    UUID id = UUID.randomUUID();

    // when
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(service.getResultClass())
    )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    T found = service.findOne(id);

    // then
    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET),
            entityCaptor.capture(), eq(service.getResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + id;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(found, is(nullValue()));

    assertAuthHeader(entityCaptor.getValue());
    assertThat(entityCaptor.getValue().getBody(), is(nullValue()));
  }

  @Test(expected = DataRetrievalException.class)
  public void shouldThrowExceptionIfThereIsOtherProblemWithFindingById() throws Exception {
    // given
    BaseReferenceDataService<T> service = prepareService();
    UUID id = UUID.randomUUID();

    // when
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(service.getResultClass())
    )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    service.findOne(id);
  }

  @Override
  protected BaseReferenceDataService<T> prepareService() {
    BaseCommunicationService service = super.prepareService();

    ReflectionTestUtils.setField(service, "referenceDataUrl", "http://localhost/referencedata");

    return (BaseReferenceDataService<T>) service;
  }

  protected abstract BaseReferenceDataService<T> getService();

  abstract T generateInstance();
}
