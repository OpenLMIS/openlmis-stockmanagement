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

package org.openlmis.stockmanagement.interceptor;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;

public class MvcInterceptorTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private HttpServletRequest request;

  private MvcInterceptor interceptor;
  private Map<String, String[]> parameterMaps;

  @Before
  public void setUp() {
    interceptor = new MvcInterceptor();
    parameterMaps = new HashMap<>();
    given(request.getParameterMap()).willReturn(parameterMaps);
    parameterMaps.put("page", new String[]{"0"});
  }

  @Test
  public void shouldThrowExceptionIfNotPositiveSize() {
    // given
    parameterMaps.put("size", new String[]{"-1"});

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(MessageKeys.ERROR_SIZE_NOT_POSITIVE);

    // when
    interceptor.preHandle(request, null, null);
  }

  @Test
  public void shouldThrowExceptionIfSizeNull() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(MessageKeys.ERROR_SIZE_NULL);

    // when
    interceptor.preHandle(request, null, null);
  }

  @Test
  public void shouldNotThrowExceptionIfNotPositivePage() {
    // given
    parameterMaps.put("size", new String[]{"10"});

    // when
    boolean validate = interceptor.preHandle(request, null, null);

    // then
    assertTrue(validate);
  }

  @Test
  public void shouldNotThrowExceptionIfPageAndSizeNull() {
    //given
    parameterMaps.put("page", null);

    // when
    boolean validate = interceptor.preHandle(request, null, null);

    // then
    assertTrue(validate);
  }

  @Test
  public void shouldNotThrowExceptionIfPositivePageAndSize() {
    // given
    parameterMaps.put("page", new String[]{"1"});
    parameterMaps.put("size", new String[]{"10"});

    // when
    boolean validate = interceptor.preHandle(request, null, null);

    // then
    assertTrue(validate);
  }

}