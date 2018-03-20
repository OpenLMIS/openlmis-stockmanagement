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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.web.util.UriUtils.encodeQueryParam;

import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.openlmis.stockmanagement.service.RequestHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

@SuppressWarnings("PMD.TooManyMethods")
public class RequestHelperTest {
  private static final String URL = "http://localhost";
  private static final String BEARER = "Bearer ";

  private static final int MAX_URL_LENGTH = 2000;

  @Test
  public void shouldCreateUriWithoutParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL, RequestParameters.init());
    assertThat(uri.getQuery(), is(nullValue()));
  }

  @Test
  public void shouldCreateUriWithNullParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL);
    assertThat(uri.getQuery(), is(nullValue()));
  }

  @Test
  public void shouldCreateUriWithParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL, RequestParameters.init().set("a", "b"));
    assertThat(uri.getQuery(), is("a=b"));
  }

  @Test
  public void shouldCreateUriWithEncodedParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL, RequestParameters.init().set("a", "b c"));
    assertThat(uri.getQuery(), is("a=b%20c"));
  }

  @Test
  public void shouldCreateEntityWithHeaders() {
    String body = "test";
    String token = "token";

    RequestHeaders headers = RequestHeaders.init()
        .set(HttpHeaders.AUTHORIZATION, BEARER + token);
    HttpEntity<String> entity = RequestHelper.createEntity(body, headers);

    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
        is(singletonList(BEARER + token)));
    assertThat(entity.getBody(), is(body));
  }

  @Test
  public void shouldCreateEntityWithNoBody() {
    String token = "token";

    RequestHeaders headers = RequestHeaders.init()
        .set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    HttpEntity<String> entity = RequestHelper.createEntity(headers);

    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
        is(singletonList(BEARER + token)));
  }

  @Test
  public void shouldSplitRequestIfItTooLong() throws UnsupportedEncodingException {
    // no split | first split | second split
    //    1     |      1      |      1
    //    2     |      1      |      1
    //    3     |      1      |      1
    //    4     |      1      |      2
    //    5     |      1      |      2
    //    6     |      2      |      3
    //    7     |      2      |      3
    //    8     |      2      |      4
    //    9     |      2      |      4
    List<String> queryParamValues = Lists.newArrayList(
        randomString(), randomString(), randomString(), randomString(),
        randomString(), randomString(), randomString(), randomString(),
        randomString()
    );

    URI[] uri = RequestHelper.splitRequest(
        URL, RequestParameters.init().set("a", queryParamValues), MAX_URL_LENGTH
    );
    assertThat(uri.length, is(4));

    assertThat(uri[0].toString(), startsWith(URL));
    assertThat(uri[0].toString(), containsString("a=" + queryParamValues.get(0)));
    assertThat(uri[0].toString(), containsString("a=" + queryParamValues.get(1)));
    assertThat(uri[0].toString(), containsString("a=" + queryParamValues.get(2)));

    assertThat(uri[1].toString(), startsWith(URL));
    assertThat(uri[1].toString(), containsString("a=" + queryParamValues.get(3)));
    assertThat(uri[1].toString(), containsString("a=" + queryParamValues.get(4)));

    assertThat(uri[2].toString(), startsWith(URL));
    assertThat(uri[2].toString(), containsString("a=" + queryParamValues.get(5)));
    assertThat(uri[2].toString(), containsString("a=" + queryParamValues.get(6)));

    assertThat(uri[3].toString(), startsWith(URL));
    assertThat(uri[3].toString(), containsString("a=" + queryParamValues.get(7)));
    assertThat(uri[3].toString(), containsString("a=" + queryParamValues.get(8)));
  }

  @Test
  public void shouldNotSplitRequestIfQueryParamsCouldNotBeSplit() {
    String queryParamValue = RandomStringUtils.randomAlphabetic(2500);
    URI[] uri = RequestHelper
        .splitRequest(URL, RequestParameters.init().set("a", queryParamValue), MAX_URL_LENGTH);
    assertThat(uri.length, is(1));
    assertThat(uri[0].toString(), startsWith(URL));
    assertThat(uri[0].toString(), containsString("a=" + queryParamValue));
  }

  @Test
  public void shouldNotSplitRequestIfLengthIsInRange() {
    URI[] uri = RequestHelper
        .splitRequest(URL, RequestParameters.init().set("a", "b"), MAX_URL_LENGTH);
    assertThat(uri.length, is(1));
    assertThat(uri[0].toString(), startsWith(URL));
    assertThat(uri[0].toString(), containsString("a=b"));
  }

  private String randomString() throws UnsupportedEncodingException {
    return encodeQueryParam(RandomStringUtils.randomAlphabetic(500), UTF_8.name());
  }
}
