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


import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;

import org.openlmis.stockmanagement.dto.ResultDto;
import org.openlmis.stockmanagement.dto.RightDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService {

  public static final String STOCK_CARD_TEMPLATES_MANAGE = "STOCK_CARD_TEMPLATES_MANAGE";
  public static final String REASONS_MANAGE = "STOCK_CARD_LINE_ITEM_REASONS_MANAGE";
  public static final String ORGANIZATIONS_MANAGE = "ORGANIZATIONS_MANAGE";

  public static final String STOCK_EVENT_CREATE = "STOCK_EVENT_CREATE";

  public static final String STOCK_SOURCES_VIEW = "STOCK_SOURCES_VIEW";

  public static final String STOCK_SOURCES_MANAGE = "STOCK_SOURCES_MANAGE";

  public static final String STOCK_DESTINATIONS_VIEW = "STOCK_DESTINATIONS_VIEW";

  public static final String STOCK_CARD_LINE_ITEM_REASONS_VIEW
      = "STOCK_CARD_LINE_ITEM_REASONS_VIEW";

  //assumption: if a user can view requisition then we assume this user can view stock card too.
  public static final String STOCK_CARD_VIEW = "REQUISITION_VIEW";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private ProgramFacilityTypePermissionService programFacilityTypePermissionService;

  /**
   * Checks if current user has permission to submit a stock card template.
   *
   * @throws PermissionMessageException if the current user has not a permission.
   */
  public void canCreateStockCardTemplate() {
    hasPermission(STOCK_CARD_TEMPLATES_MANAGE, null, null, null);
  }

  /**
   * Checks if current user has permission to create a stock event.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canCreateStockEvent(UUID programId, UUID facilityId) {
    hasPermission(STOCK_EVENT_CREATE, programId, facilityId, null);
  }

  /**
   * Checks if current user has permission to view stock card.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canViewStockCard(UUID programId, UUID facilityId) {
    hasPermission(STOCK_CARD_VIEW, programId, facilityId, null);
  }

  /**
   * Checks if current user has permission to view stock sources.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   */
  public void canViewStockSource(UUID program, UUID facilityType) {
    canViewStockAssignable(STOCK_SOURCES_VIEW, program, facilityType);
  }

  public void canManageStockSource() {
    hasPermission(STOCK_SOURCES_MANAGE, null, null, null);
  }

  /**
   * Checks if current user has permission to view stock destinations.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   */
  public void canViewStockDestinations(UUID program, UUID facilityType) {
    canViewStockAssignable(STOCK_DESTINATIONS_VIEW, program, facilityType);
  }

  /**
   * Checks if current user has permission to view stock card line item reasons.
   *
   * @param program      program ID
   * @param facilityType facility type ID
   */
  public void canViewReasons(UUID program, UUID facilityType) {
    canViewStockAssignable(STOCK_CARD_LINE_ITEM_REASONS_VIEW, program, facilityType);
  }

  public void canManageReasons() {
    hasPermission(REASONS_MANAGE, null, null, null);
  }

  public void canManageOrganizations() {
    hasPermission(ORGANIZATIONS_MANAGE, null, null, null);
  }

  private void hasPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    ResultDto<Boolean> result = getRightResult(rightName, program, facility, warehouse);
    if (null == result || !result.getResult()) {
      throw new PermissionMessageException(new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName));
    }
  }

  private void canViewStockAssignable(String rightName, UUID program, UUID facilityType) {
    ResultDto<Boolean> result = getRightResult(rightName, null, null, null);
    if (null == result || !result.getResult()) {
      programFacilityTypePermissionService.checkProgramFacility(program, facilityType);
    }
  }

  private ResultDto<Boolean> getRightResult(
      String rightName, UUID program, UUID facility, UUID warehouse) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    return userReferenceDataService.hasRight(
        user.getId(), right.getId(), program, facility, warehouse
    );
  }
}
