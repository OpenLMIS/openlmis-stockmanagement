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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.openlmis.stockmanagement.web.utils.WireMockResponses.MOCK_CHECK_RESULT;
import static org.openlmis.stockmanagement.web.utils.WireMockResponses.MOCK_TOKEN_REQUEST_RESPONSE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardLineItemReasonService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext
@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseWebIntegrationTest {
  protected static final String BASE_URL = System.getenv("BASE_URL");
  protected static final String CONTENT_TYPE = "Content-Type";
  protected static final String APPLICATION_JSON = MediaType.APPLICATION_JSON_VALUE;
  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";

  static final String ID_FIELD = "id";

  static final String MESSAGE_KEY = "messageKey";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(80);

  @MockBean
  protected AuthenticationHelper authenticationHelper;

  @MockBean
  protected StockCardLineItemReasonService stockCardLineItemReasonService;

  @MockBean
  protected StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @MockBean
  protected PermissionService permissionService;

  protected RestAssuredClient restAssured;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int serverPort;

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

  protected void mockUserAuthenticated() {
    UserDto user = new UserDto();
    user.setId(UUID.randomUUID());
    user.setFirstName("admin");
    user.setLastName("strator");
    user.setEmail("admin@openlmis.org");

    given(authenticationHelper.getCurrentUser()).willReturn(user);
  }

  protected UUID anyUuid() {
    return any(UUID.class);
  }

  protected String getTokenHeader() {
    return "Bearer " + UUID.randomUUID().toString();
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
}
