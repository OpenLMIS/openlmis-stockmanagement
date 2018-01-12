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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.openlmis.stockmanagement.security.CustomUserAuthenticationConverter.REFERENCE_DATA_USER_ID;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class CustomUserAuthenticationConverterTest {

  private UserAuthenticationConverter userAuthenticationConverter;
  private UUID userId = UUID.randomUUID();

  @Before
  public void setUp() {
    userAuthenticationConverter = new CustomUserAuthenticationConverter();
    userId = UUID.randomUUID();

  }

  @Test(expected = IllegalArgumentException.class)
  public void convertUserAuthentication() throws Exception {
    userAuthenticationConverter.convertUserAuthentication(mock(Authentication.class));
  }

  @Test
  public void shouldExtractAuthenticationWithPrincipalWithoutAuthorities() {
    Authentication authentication = userAuthenticationConverter.extractAuthentication(
        ImmutableMap.of(REFERENCE_DATA_USER_ID, userId.toString()));

    checkAuthentication(userId, authentication);
    assertTrue(authentication.getAuthorities().isEmpty());
  }

  @Test
  public void shouldExtractAuthenticationWithPrincipalAndCommaSeparatedAuthorities() {
    Authentication authentication = userAuthenticationConverter.extractAuthentication(
        ImmutableMap.of(
            REFERENCE_DATA_USER_ID, userId.toString(),
            UserAuthenticationConverter.AUTHORITIES, "one,two,three"));

    checkAuthentication(userId, authentication);
    assertEquals(3, authentication.getAuthorities().size());
  }

  @Test
  public void shouldExtractAuthenticationWithPrincipalAndCollectionAuthorities() {
    Authentication authentication = userAuthenticationConverter.extractAuthentication(
        ImmutableMap.of(
            REFERENCE_DATA_USER_ID, userId.toString(),
            UserAuthenticationConverter.AUTHORITIES, Arrays.asList("one", "two")));

    checkAuthentication(userId, authentication);
    assertEquals(2, authentication.getAuthorities().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotExtractAuthenticationWhenAuthoritiesAreNotInSupportedFormat() {
    userAuthenticationConverter.extractAuthentication(
        ImmutableMap.of(
            REFERENCE_DATA_USER_ID, userId.toString(),
            UserAuthenticationConverter.AUTHORITIES, 10));
  }

  @Test
  public void shouldReturnNullWhenMapDoesNotContainPrincipal() {
    Authentication authentication =
        userAuthenticationConverter.extractAuthentication(Collections.emptyMap());

    assertNull(authentication);
  }

  private void checkAuthentication(UUID userId, Authentication authentication) {
    assertEquals(userId, authentication.getPrincipal());
    assertEquals("N/A", authentication.getCredentials());
    assertTrue(authentication.isAuthenticated());
  }

}