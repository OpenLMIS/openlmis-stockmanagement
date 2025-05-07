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

package org.openlmis.stockmanagement.security;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CustomTokenServicesTest {

  private static final String CLIENT_ID = "client-id-1234";
  private static final String CLIENT_SECRET = "client-secret-1234";
  private static final String CLIENT_CHECK_TOKEN_ENDPOINT_URI = "/oauth/check_token";
  private static final String ACCESS_TOKEN = "access-token-1234";
  private static final int INVALID_TOKEN_RETRY_LIMIT = 3;

  @Mock
  private RestTemplate restTemplate;

  private CustomTokenServices customTokenServices;

  @Before
  public void setUp() {
    this.customTokenServices = new CustomTokenServices(INVALID_TOKEN_RETRY_LIMIT);
    this.customTokenServices.setClientId(CLIENT_ID);
    this.customTokenServices.setClientSecret(CLIENT_SECRET);
    this.customTokenServices.setCheckTokenEndpointUrl(CLIENT_CHECK_TOKEN_ENDPOINT_URI);
  }

  @Test
  public void shouldSuccessfullyAuthenticateWhenResponseContainsActiveTrue() throws Exception {
    Map responseAttributes = new HashMap();
    responseAttributes.put("active", true);
    responseAttributes.put("client_id", CLIENT_ID);
    ResponseEntity<Map> response = new ResponseEntity<>(responseAttributes, HttpStatus.OK);

    when(restTemplate.exchange(
        anyString(), ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(HttpEntity.class),
        ArgumentMatchers.any(Class.class)
    )).thenReturn(response);
    this.customTokenServices.setRestTemplate(restTemplate);

    OAuth2Authentication authentication = this.customTokenServices.loadAuthentication(ACCESS_TOKEN);
    assertNotNull(authentication);
  }

  @Test(expected = InvalidTokenException.class)
  public void shouldThrowInvalidTokenException() throws Exception {
    Map responseAttributes = new HashMap();
    responseAttributes.put("error", "no_active_token");
    ResponseEntity<Map> response = new ResponseEntity<>(responseAttributes, HttpStatus.OK);

    when(restTemplate.exchange(
        anyString(), ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(HttpEntity.class),
        ArgumentMatchers.any(Class.class)
    )).thenReturn(response);

    this.customTokenServices.setRestTemplate(restTemplate);
    this.customTokenServices.loadAuthentication(ACCESS_TOKEN);
  }
}
