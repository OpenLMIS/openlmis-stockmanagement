package org.openlmis.stockmanagement.service;


import org.openlmis.stockmanagement.dto.BooleanResultDto;
import org.openlmis.stockmanagement.dto.RightDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class PermissionService {

  static final String MANAGE_STOCK_CARD_TEMPLATES = "MANAGE_STOCK_CARD_TEMPLATES";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;



  /**
   * Checks if current user has permission to submit a stock card template.
   *
   * @throws MissingPermissionException if the current user has not a permission.
   */
  public void canCreateStockCardTemplate() throws MissingPermissionException {
    hasPermission(MANAGE_STOCK_CARD_TEMPLATES, null, null, null);
  }

  private void hasPermission(String rightName, UUID program, UUID facility, UUID warehouse)
      throws MissingPermissionException {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    BooleanResultDto result = userReferenceDataService.hasRight(
        user.getId(), right.getId(), program, facility, warehouse
    );

    if (null == result || !result.isResult()) {
      throw new MissingPermissionException(rightName);
    }
  }

}
