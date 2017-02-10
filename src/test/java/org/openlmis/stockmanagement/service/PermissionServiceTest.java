package org.openlmis.stockmanagement.service;

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

import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_CARD_TEMPLATES_MANAGE;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private UserReferenceDataService userReferenceDataService;

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