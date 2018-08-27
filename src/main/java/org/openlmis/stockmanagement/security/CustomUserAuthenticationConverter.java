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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

/**
 * Implementation of {@link UserAuthenticationConverter}. Converts from an Authentication
 * using only referencedata user id.
 *
 */
public class CustomUserAuthenticationConverter implements UserAuthenticationConverter {

  public static final String REFERENCE_DATA_USER_ID = "referenceDataUserId";

  public Map<String, ?> convertUserAuthentication(Authentication authentication) {
    throw new IllegalArgumentException("Convert User Authentication is not supported");
  }

  /**
   * {@inheritDoc}.
   */
  public Authentication extractAuthentication(Map<String, ?> map) {
    if (map.containsKey(REFERENCE_DATA_USER_ID)) {
      UUID principal = UUID.fromString((String)map.get(REFERENCE_DATA_USER_ID));
      Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
      return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
    }
    return null;
  }

  private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
    if (!map.containsKey(AUTHORITIES)) {
      return AuthorityUtils.NO_AUTHORITIES;
    }
    Object authorities = map.get(AUTHORITIES);
    if (authorities instanceof String) {
      return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
    }
    if (authorities instanceof Collection) {
      return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
          .collectionToCommaDelimitedString((Collection<?>) authorities));
    }
    throw new IllegalArgumentException("Authorities must be either a String or a Collection");
  }
}
