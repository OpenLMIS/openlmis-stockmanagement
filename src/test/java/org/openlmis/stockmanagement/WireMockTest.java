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

package org.openlmis.stockmanagement;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class WireMockTest {

  Logger logger = LoggerFactory.getLogger(WireMockTest.class);

  private class WireMockTestClient {
    String resourceUrl = "http://localhost:8080/api/resource";

    String getResource() {
      RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(resourceUrl, String.class);
    }

    void postMessage(String message) {
      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      String messageJson = "{\"message\": \"" + message + "\"}";
      HttpEntity<String> entity = new HttpEntity<>(messageJson, headers);
      try {
        restTemplate.postForEntity(resourceUrl, entity, Object.class);
      } catch (HttpClientErrorException exception) {
        logger.debug(exception.getMessage());
      }
    }
  }

  @Rule
  public WireMockRule wireMockRule = new WireMockRule();

  @Test
  public void wireMockTest() {
    WireMockTestClient testClient = new WireMockTestClient();
    String responseJson = "{\"response\": \"Some content\"}";

    stubFor(get(urlEqualTo("/api/resource"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson)));

    String result = testClient.getResource();

    Assert.assertEquals(result, responseJson);

    testClient.postMessage("1234");

    verify(postRequestedFor(urlMatching("/api/resource"))
        .withRequestBody(matching(".*1234.*"))
        .withHeader("Content-Type", matching("application/json")));
  }
}
