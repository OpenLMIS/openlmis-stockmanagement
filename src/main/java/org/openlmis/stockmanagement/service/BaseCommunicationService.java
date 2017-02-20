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

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseCommunicationService {
  protected static final String ACCESS_TOKEN = "access_token";

  protected RestOperations restTemplate = new RestTemplate();

  @Value("${auth.server.clientId}")
  private String clientId;

  @Value("${auth.server.clientSecret}")
  private String clientSecret;

  @Value("${auth.server.authorizationUrl}")
  private String authorizationUrl;

  public BaseCommunicationService() {
    this.restTemplate = new RestTemplate();
  }

  protected String obtainAccessToken() {
    String plainCreds = clientId + ":" + clientSecret;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);

    HttpEntity<String> request = new HttpEntity<>(headers);

    Map<String, Object> params = new HashMap<>();
    params.put("grant_type", "client_credentials");

    ResponseEntity<?> response = restTemplate.exchange(
            buildUri(authorizationUrl, params), HttpMethod.POST, request, Object.class);


    return ((Map<String, String>) response.getBody()).get(ACCESS_TOKEN);
  }

  protected URI buildUri(String url, Map<String, ?> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    params.entrySet().forEach(e -> builder.queryParam(e.getKey(), e.getValue()));

    return builder.build(true).toUri();
  }

}
