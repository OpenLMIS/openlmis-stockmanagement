package org.openlmis.stockmanagement.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseWebTest extends BaseTest {

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

  @Autowired
  private WebApplicationContext context;
  protected MockMvc mvc;

  @Before
  public void setup() {
    mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
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

  protected String objectToJsonString(Object obj) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    return mapper.writeValueAsString(obj);
  }
}
