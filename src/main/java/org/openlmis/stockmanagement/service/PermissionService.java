package org.openlmis.stockmanagement.service;


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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;

@Service
public class PermissionService {

  public static final String STOCK_CARD_TEMPLATES_MANAGE = "STOCK_CARD_TEMPLATES_MANAGE";
  public static final String STOCK_EVENT_CREATE = "STOCK_EVENT_CREATE";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;


  /**
   * Checks if current user has permission to submit a stock card template.
   *
   * @throws PermissionMessageException if the current user has not a permission.
   */
  public void canCreateStockCardTemplate() {
    hasPermission(STOCK_CARD_TEMPLATES_MANAGE, null, null, null);
  }

  public void canCreateStockEvent(UUID programId, UUID facilityId) {
    hasPermission(STOCK_EVENT_CREATE, programId, facilityId, null);
  }

  private void hasPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(
        user.getId(), right.getId(), program, facility, warehouse
    );

    if (null == result || !result.getResult()) {
      throw new PermissionMessageException(new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName));
    }
  }

}
