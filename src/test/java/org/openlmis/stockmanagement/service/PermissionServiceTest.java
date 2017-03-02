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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_CARD_LINE_ITEM_REASONS_VIEW;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_CARD_TEMPLATES_MANAGE;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_DESTINATIONS_VIEW;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_SOURCES_VIEW;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.ResultDto;
import org.openlmis.stockmanagement.dto.RightDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.utils.Message;

import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private ProgramFacilityTypePermissionService programFacilityTypePermissionService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @InjectMocks
  private PermissionService permissionService;

  @Mock
  private UserDto user;

  @Mock
  private RightDto manageStockCardTemplatesRight;


  private UUID userId = UUID.randomUUID();
  private UUID manageStockCardTemplatesRightId = UUID.randomUUID();


  @Before
  public void setUp() {


    when(user.getId()).thenReturn(userId);

    when(manageStockCardTemplatesRight.getId()).thenReturn(manageStockCardTemplatesRightId);

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    when(authenticationHelper.getRight(STOCK_CARD_TEMPLATES_MANAGE))
        .thenReturn(manageStockCardTemplatesRight);

    when(authenticationHelper.getRight(STOCK_SOURCES_VIEW)).thenReturn(new RightDto());
    when(authenticationHelper.getRight(STOCK_DESTINATIONS_VIEW)).thenReturn(new RightDto());
    when(authenticationHelper.getRight(STOCK_CARD_LINE_ITEM_REASONS_VIEW))
        .thenReturn(new RightDto());
  }

  @Test
  public void canCreateStockCardTemplates() throws Exception {
    hasRight(manageStockCardTemplatesRightId, true);
    permissionService.canCreateStockCardTemplate();
    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyCreateStockCardTemplatesRight(order,
        STOCK_CARD_TEMPLATES_MANAGE, manageStockCardTemplatesRightId);
  }

  @Test
  public void cannotCreateStockCardTemplates() throws Exception {
    expectException(STOCK_CARD_TEMPLATES_MANAGE);
    permissionService.canCreateStockCardTemplate();
  }

  @Test
  public void canViewStockEventAssignable() throws Exception {
    hasRight(UUID.randomUUID(), true);
    UUID program = UUID.randomUUID();
    UUID facilityType = UUID.randomUUID();

    permissionService.canViewStockSource(program, facilityType);
    permissionService.canViewStockDestinations(program, facilityType);
    permissionService.canViewReasons(program, facilityType);
  }

  @Test
  public void cannotViewStockSources() throws Exception {
    checkViewRight(STOCK_SOURCES_VIEW);
  }

  @Test
  public void cannotViewStockDestinations() throws Exception {
    checkViewRight(STOCK_DESTINATIONS_VIEW);
  }

  @Test
  public void cannotViewStockCardLineItemReasons() throws Exception {
    checkViewRight(STOCK_CARD_LINE_ITEM_REASONS_VIEW);
  }

  private void checkViewRight(String rightName) {
    expectException(rightName);

    UUID program = UUID.randomUUID();
    UUID facilityType = UUID.randomUUID();
    doThrow(new PermissionMessageException(
        new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName)))
        .when(programFacilityTypePermissionService)
        .checkProgramFacility(program, facilityType);

    permissionService.canViewStockDestinations(program, facilityType);
  }

  private void hasRight(UUID rightId, boolean assign) {
    ResultDto<Boolean> resultDto = new ResultDto<>(assign);
    when(userReferenceDataService
        .hasRight(userId, rightId, null, null, null)
    ).thenReturn(resultDto);
  }

  private void expectException(String rightName) {
    exception.expect(PermissionMessageException.class);
    exception.expectMessage(ERROR_NO_FOLLOWING_PERMISSION + ": " + rightName);
  }

  private void verifyCreateStockCardTemplatesRight(InOrder order, String rightName, UUID rightId) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, null, null,
        null);
  }

}