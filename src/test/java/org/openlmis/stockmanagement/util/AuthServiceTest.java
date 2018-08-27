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

package org.openlmis.stockmanagement.util;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.service.AuthService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {
  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String AUTHORIZATION_URL = "http://localhost/auth/oauth/token";
  private static final URI AUTHORIZATION_URI = URI.create(
      AUTHORIZATION_URL + "?grant_type=client_credentials"
  );

  @Mock
  private RestTemplate restTemplate;

  @Captor
  private ArgumentCaptor<HttpEntity<String>> entityStringCaptor;

  private AuthService authService;

  @Before
  public void setUp() throws Exception {
    authService = new AuthService();
    ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(authService, "clientId", "trusted-client");
    ReflectionTestUtils.setField(authService, "clientSecret", "secret");
    ReflectionTestUtils.setField(authService, "authorizationUrl", AUTHORIZATION_URL);
  }

  @Test
  public void shouldObtainAccessToken() throws Exception {
    ResponseEntity<Object> response = mock(ResponseEntity.class);
    Map<String, String> body = ImmutableMap.of(AuthService.ACCESS_TOKEN, TOKEN);

    when(restTemplate.exchange(
        eq(AUTHORIZATION_URI), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)
    )).thenReturn(response);

    when(response.getBody()).thenReturn(body);

    String token = authService.obtainAccessToken();
    assertThat(token, is(equalTo(TOKEN)));

    verify(restTemplate).exchange(
        eq(AUTHORIZATION_URI), eq(HttpMethod.POST), entityStringCaptor.capture(), eq(Object.class)
    );

    HttpEntity<String> entity = entityStringCaptor.getValue();
    assertThat(
        entity.getHeaders().get("Authorization"),
        contains("Basic dHJ1c3RlZC1jbGllbnQ6c2VjcmV0")
    );
  }
}