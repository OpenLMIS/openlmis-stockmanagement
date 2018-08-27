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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.openlmis.stockmanagement.web.utils.WireMockResponses.MOCK_TOKEN_REQUEST_RESPONSE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseWebTest extends BaseIntegrationTest {

  protected static final String BASE_URL = System.getenv("BASE_URL");
  public static final String ACCESS_TOKEN = "access_token";
  public static final String ACCESS_TOKEN_VALUE = "xxx";

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String APPLICATION_JSON = "application/json";
  private static final String MOCK_CHECK_RESULT = "{"
          + "  \"aud\": [\n"
          + "    \"stockmanagement\"\n"
          + "  ],\n"
          + "  \"user_name\": \"admin\",\n"
          + "  \"referenceDataUserId\": \"35316636-6264-6331-2d34-3933322d3462\",\n"
          + "  \"scope\": [\n"
          + "    \"read\",\n"
          + "    \"write\"\n"
          + "  ],\n"
          + "  \"exp\": 1474500343,\n"
          + "  \"authorities\": [\n"
          + "    \"USER\",\n"
          + "    \"ADMIN\"\n"
          + "  ],\n"
          + "  \"client_id\": \"trusted-client\"\n"
          + "}";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8888);

  @LocalServerPort
  private int serverPort;

  @Autowired
  private WebApplicationContext context;
  protected MockMvc mvc;

  @Autowired
  protected ObjectMapper objectMapper;

  protected RestAssuredClient restAssured;

  @Before
  public void setup() {
    mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
  }

  /**
   * Method called to initialize basic resources after the object is created.
   */
  @PostConstruct
  public void init() {
    mockExternalAuthorization();

    RestAssured.baseURI = BASE_URL;
    RestAssured.port = serverPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig().jackson2ObjectMapperFactory((clazz, charset) -> objectMapper)
    );
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    RamlDefinition ramlDefinition = RamlLoaders.fromClasspath()
        .load("api-definition-raml.yaml").ignoringXheaders();
    restAssured = ramlDefinition.createRestAssured();
  }

  /**
   * Create instance of base web test, mock auth api.
   */
  public BaseWebTest() {
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
            .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody(MOCK_CHECK_RESULT)));
  }

  protected String getTokenHeader() {
    return "Bearer " + UUID.randomUUID().toString();
  }

  protected String objectToJsonString(Object obj) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    return mapper.writeValueAsString(obj);
  }

  protected void mockExternalAuthorization() {
    // This mocks the auth check to always return valid admin credentials.
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_CHECK_RESULT)));

    // This mocks the auth token request response
    wireMockRule.stubFor(post(urlPathEqualTo("/api/oauth/token?grant_type=client_credentials"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_TOKEN_REQUEST_RESPONSE)));

  }

  static class SaveAnswer<T extends BaseEntity> implements Answer<T> {

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
      T obj = (T) invocation.getArguments()[0];

      if (null == obj) {
        return null;
      }

      if (null == obj.getId()) {
        obj.setId(UUID.randomUUID());
      }

      return obj;
    }

  }
}
