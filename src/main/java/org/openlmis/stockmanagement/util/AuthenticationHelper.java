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

import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private RightReferenceDataService rightReferenceDataService;

  /**
   * Method returns current user based on Spring context
   * and fetches his data from reference-data service.
   *
   * @return UserDto entity of current user.
   * @throws AuthenticationException if user cannot be found.
   */
  public UserDto getCurrentUser() {
    String username =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UserDto user = userReferenceDataService.findUser(username);

    if (user == null) {
      throw new AuthenticationException("User with name \"" + username + "\" not found.");
    }

    return user;
  }

  /**
   * Method returns a correct right and fetches his data from reference-data service.
   *
   * @param name right name
   * @return RightDto entity of right.
   * @throws AuthenticationException if right cannot be found.
   */
  public RightDto getRight(String name) {
    RightDto right = rightReferenceDataService.findRight(name);

    if (null == right) {
      throw new AuthenticationException("Right with name \"" + name + "\" not found");
    }

    return right;
  }
}
