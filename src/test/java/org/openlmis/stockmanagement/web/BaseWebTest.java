package org.openlmis.stockmanagement.web;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseWebTest {

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

  @Autowired
  private WebApplicationContext wac;
  protected MockMvc mvc;

  @Before
  public void setup() {
    mvc = webAppContextSetup(wac).build();
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
}
