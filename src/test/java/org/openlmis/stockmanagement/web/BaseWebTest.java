package org.openlmis.stockmanagement.web;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class BaseWebTest {

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
  public WireMockRule wireMockRule = new WireMockRule(80);

  /**
   * Create instance of base web test, mock auth api.
   */
  public BaseWebTest() {
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
            .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody(MOCK_CHECK_RESULT)));
  }
}
