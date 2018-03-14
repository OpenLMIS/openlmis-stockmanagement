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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.UUID;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.testutils.ObjectGenerator;
import org.openlmis.stockmanagement.util.DynamicPageTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseCommunicationServiceTest<T> {
  private static final String TOKEN = UUID.randomUUID().toString();

  @Mock
  protected RestTemplate restTemplate;

  @Mock
  protected AuthService authService;

  @Mock
  private ResponseEntity<T[]> arrayResponse;

  @Captor
  protected ArgumentCaptor<URI> uriCaptor;

  @Captor
  protected ArgumentCaptor<HttpEntity<String>> entityCaptor;

  @Before
  public void setUp() throws Exception {
    mockAuth();
  }

  @After
  public void tearDown() throws Exception {
    checkAuth();
  }

  protected abstract BaseCommunicationService<T> getService();

  protected abstract T generateInstance();

  protected BaseCommunicationService<T> prepareService() {
    BaseCommunicationService<T> service = getService();
    ReflectionTestUtils.setField(service, "authService", authService);
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

    return service;
  }

  protected void assertAuthHeader(HttpEntity entity) {
    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            is(singletonList("Bearer " + TOKEN)));
  }

  private void mockAuth() {
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
  }

  private void checkAuth() {
    verify(authService, atLeastOnce()).obtainAccessToken();
  }

  protected T mockPageResponseEntityAndGetDto() {
    T dto = ObjectGenerator.of((Class<T>) generateInstance().getClass());
    mockPageResponseEntity(dto);
    return dto;
  }

  protected void mockPageResponseEntity(Object dto) {
    ResponseEntity<Page<T>> response = stubRestTemplateAndGetPageResponseEntity();

    when(response.getBody())
        .thenReturn((Page<T>) new PageImpl<>(ImmutableList.of(dto)));
  }

  private ResponseEntity<Page<T>> stubRestTemplateAndGetPageResponseEntity() {
    ResponseEntity<Page<T>> response = mock(ResponseEntity.class);
    when(restTemplate.exchange(
        any(URI.class),
        any(HttpMethod.class),
        any(HttpEntity.class),
        any(DynamicPageTypeReference.class)))
        .thenReturn(response);

    return response;
  }

  protected void mockArrayResponse(T[] responseArray) {
    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
        any(Class.class))).thenReturn(arrayResponse);
    when(arrayResponse.getBody()).thenReturn(responseArray);
  }
}
