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

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

public class CustomTokenServices extends RemoteTokenServices {

  private int invalidTokenRetryLimit;

  public CustomTokenServices(int invalidTokenRetryLimit) {
    super();
    this.invalidTokenRetryLimit = invalidTokenRetryLimit;
  }

  @Override
  public OAuth2Authentication loadAuthentication(String accessToken) {
    return loadAuthentication(accessToken, 0);
  }

  private OAuth2Authentication loadAuthentication(String accessToken, int attempt) {
    try {
      return super.loadAuthentication(accessToken);
    } catch (InvalidTokenException e) {
      if (attempt < invalidTokenRetryLimit) {
        attempt++;
        logger.debug("Retrying authentication load. Retry number: " + attempt);
        return loadAuthentication(accessToken, attempt);
      } else {
        throw e;
      }
    }
  }
}