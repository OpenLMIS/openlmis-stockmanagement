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

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseCommunicationServiceTest {
  private static final String TOKEN = UUID.randomUUID().toString();

  @Mock
  protected RestTemplate restTemplate;

  @Mock
  protected AuthService authService;

  @Mock
  @Getter
  private ResponseEntity arrayResponse;

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

  protected abstract BaseCommunicationService getService();

  protected BaseCommunicationService prepareService() {
    BaseCommunicationService service = getService();
    service.setRestTemplate(restTemplate);
    ReflectionTestUtils.setField(service, "authService", authService);

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

  protected <P> void mockArrayRequest(HttpMethod method, Class<P[]> type) {
    when(restTemplate.exchange(uriCaptor.capture(), eq(method), entityCaptor.capture(), eq(type)))
        .thenReturn(arrayResponse);
  }

  protected void mockArrayResponse(Consumer<ResponseEntity> action) {
    action.accept(arrayResponse);
  }

  protected URI getUri() {
    return uriCaptor.getValue();
  }

  protected HttpEntity getEntity() {
    HttpEntity entity = entityCaptor.getValue();
    assertThat(entity.getHeaders(), hasEntry(AUTHORIZATION, of(getTokenHeader())));

    return entity;
  }

  String getTokenHeader() {
    return "Bearer " + TOKEN;
  }
}
