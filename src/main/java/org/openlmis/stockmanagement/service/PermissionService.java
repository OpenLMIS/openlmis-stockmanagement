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

package org.openlmis.stockmanagement.service;


import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PERMISSION_CHECK_FAILED;

import org.openlmis.stockmanagement.dto.referencedata.ResultDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@SuppressWarnings("PMD.TooManyMethods")
public class PermissionService {

  public static final String STOCK_CARD_TEMPLATES_MANAGE = "STOCK_CARD_TEMPLATES_MANAGE";
  public static final String STOCK_ORGANIZATIONS_MANAGE = "STOCK_ORGANIZATIONS_MANAGE";
  public static final String REASONS_MANAGE = "STOCK_CARD_LINE_ITEM_REASONS_MANAGE";
  public static final String STOCK_SOURCES_MANAGE = "STOCK_SOURCES_MANAGE";
  public static final String STOCK_DESTINATIONS_MANAGE = "STOCK_DESTINATIONS_MANAGE";

  public static final String STOCK_INVENTORIES_EDIT = "STOCK_INVENTORIES_EDIT";
  public static final String STOCK_ADJUST = "STOCK_ADJUST";

  public static final String STOCK_CARDS_VIEW = "STOCK_CARDS_VIEW";

  static final String SYSTEM_SETTINGS_MANAGE = "SYSTEM_SETTINGS_MANAGE";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private HomeFacilityPermissionService homeFacilityPermissionService;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  /**
   * Checks if current user has permission to submit a stock card template.
   *
   * @throws PermissionMessageException if the current user has not a permission.
   */
  public void canCreateStockCardTemplate() {
    hasPermission(STOCK_CARD_TEMPLATES_MANAGE, null, null, null);
  }

  /**
   * Checks if current user has permission to do physical inventory.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canEditPhysicalInventory(UUID programId, UUID facilityId) {
    hasPermission(STOCK_INVENTORIES_EDIT, programId, facilityId, null);
  }

  /**
   * Checks if current user has permission to make stock adjustment.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canAdjustStock(UUID programId, UUID facilityId) {
    hasPermission(STOCK_ADJUST, programId, facilityId, null);
  }

  /**
   * Checks if current user has permission to view stock card.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canViewStockCard(UUID programId, UUID facilityId) {
    hasPermission(STOCK_CARDS_VIEW, programId, facilityId, null);
  }

  /**
   * Checks if current user has permission to manage valid sources assignment.
   */
  public void canManageStockSources() {
    hasPermission(STOCK_SOURCES_MANAGE, null, null, null);
  }

  /**
   * Checks if current user has permission to manage valid destinations assignment.
   */
  public void canManageStockDestinations() {
    hasPermission(STOCK_DESTINATIONS_MANAGE, null, null, null);
  }

  public void canManageReasons() {
    hasPermission(REASONS_MANAGE, null, null, null);
  }

  public void canManageOrganizations() {
    hasPermission(STOCK_ORGANIZATIONS_MANAGE, null, null, null);
  }

  /**
   * Check if user can view valid reasons. Admin with manage right can view all, regular users can
   * only view the ones that match their home facility.
   *
   * @param program      program id.
   * @param facilityType facility type id.
   */
  public void canViewValidReasons(UUID program, UUID facilityType) {
    canViewStockAssignable(REASONS_MANAGE, program, facilityType);
  }

  /**
   * Check if user can view valid sources. Admin with manage right can view all, regular users can
   * only view the ones that match their home facility.
   *
   * @param program      program id.
   * @param facilityType facility type id.
   */
  public void canViewValidSources(UUID program, UUID facilityType) {
    canViewStockAssignable(STOCK_SOURCES_MANAGE, program, facilityType);
  }

  /**
   * Check if user can view valid destinations. Admin with manage right can view all, regular users
   * can only view the ones that match their home facility.
   *
   * @param program      program id.
   * @param facilityType facility type id.
   */
  public void canViewValidDestinations(UUID program, UUID facilityType) {
    canViewStockAssignable(STOCK_DESTINATIONS_MANAGE, program, facilityType);
  }

  public void canManageSystemSettings() {
    hasPermission(SYSTEM_SETTINGS_MANAGE, null, null, null);
  }

  private void hasPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    ResultDto<Boolean> result = getRightResult(rightName, program, facility, warehouse, false);
    if (null == result || !result.getResult()) {
      throw new PermissionMessageException(
          new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName, program, facility));
    }
  }

  private void canViewStockAssignable(String rightName, UUID program, UUID facilityType) {
    ResultDto<Boolean> result = getRightResult(rightName, null, null, null, false);
    if (null == result || !result.getResult()) {
      homeFacilityPermissionService.checkProgramAndFacilityType(program, facilityType);
    }
  }

  private ResultDto<Boolean> getRightResult(String rightName, UUID program, UUID facility,
                                            UUID warehouse, boolean allowApiKey) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    return authentication.isClientOnly()
        ? checkServiceToken(allowApiKey, authentication)
        : checkUserToken(rightName, program, facility, warehouse);
  }

  private ResultDto<Boolean> checkUserToken(String rightName, UUID program, UUID facility,
                                            UUID warehouse) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);

    try {
      return userReferenceDataService.hasRight(
          user.getId(), right.getId(), program, facility, warehouse);
    } catch (HttpClientErrorException httpException) {
      throw new PermissionMessageException(
          new Message(ERROR_PERMISSION_CHECK_FAILED, httpException.getMessage()), httpException);

    }
  }

  private ResultDto<Boolean> checkServiceToken(boolean allowApiKey,
                                               OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();

    if (serviceTokenClientId.equals(clientId)) {
      return new ResultDto<>(true);
    }

    if (startsWith(clientId, apiKeyPrefix)) {
      return new ResultDto<>(allowApiKey);
    }

    return new ResultDto<>(false);
  }
}
