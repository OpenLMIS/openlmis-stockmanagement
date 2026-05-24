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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.extension.ExtensionManager;
import org.openlmis.stockmanagement.extension.point.AdjustmentReasonValidator;
import org.openlmis.stockmanagement.extension.point.ExtensionPointId;
import org.openlmis.stockmanagement.extension.point.FreeTextValidator;
import org.openlmis.stockmanagement.extension.point.UnpackKitValidator;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.validators.ApprovedOrderableValidator;
import org.openlmis.stockmanagement.validators.DefaultAdjustmentReasonValidator;
import org.openlmis.stockmanagement.validators.DefaultFreeTextValidator;
import org.openlmis.stockmanagement.validators.DefaultUnpackKitValidator;
import org.openlmis.stockmanagement.validators.DuplicateTransactionValidator;
import org.openlmis.stockmanagement.validators.LotValidator;
import org.openlmis.stockmanagement.validators.MandatoryFieldsValidator;
import org.openlmis.stockmanagement.validators.OrderableLotDuplicationValidator;
import org.openlmis.stockmanagement.validators.PhysicalInventoryAdjustmentReasonsValidator;
import org.openlmis.stockmanagement.validators.QuantityValidator;
import org.openlmis.stockmanagement.validators.ReasonExistenceValidator;
import org.openlmis.stockmanagement.validators.ReceiveIssueReasonValidator;
import org.openlmis.stockmanagement.validators.SourceDestinationAssignmentValidator;
import org.openlmis.stockmanagement.validators.SourceDestinationGeoLevelAffinityValidator;
import org.openlmis.stockmanagement.validators.StockEventVvmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventValidationsServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private StockEventValidationsService stockEventValidationsService;

  @MockBean
  private StockEventVvmValidator stockEventVvmValidator;

  @MockBean
  private ApprovedOrderableValidator approvedOrderableValidator;

  @MockBean
  private SourceDestinationAssignmentValidator sourceDestinationAssignmentValidator;

  @MockBean
  private MandatoryFieldsValidator mandatoryFieldsValidator;

  @MockBean
  private ReceiveIssueReasonValidator receiveIssueReasonValidator;

  @MockBean
  private DefaultAdjustmentReasonValidator adjustmentReasonValidator;

  @MockBean
  private DefaultFreeTextValidator freeTextValidator;

  @MockBean
  private QuantityValidator quantityValidator;

  @MockBean
  private LotValidator lotValidator;

  @MockBean
  private ReasonExistenceValidator reasonExistenceValidator;

  @MockBean
  private OrderableLotDuplicationValidator orderableLotDuplicationValidator;

  @MockBean
  private PhysicalInventoryAdjustmentReasonsValidator physicalInventoryReasonsValidator;

  @MockBean
  private DefaultUnpackKitValidator unpackKitValidator;

  @MockBean
  private SourceDestinationGeoLevelAffinityValidator sourceDestinationGeoLeveLAffinityValidator;

  @MockBean
  private DuplicateTransactionValidator duplicateTransactionValidator;

  @MockBean
  private ExtensionManager extensionManager;

  @Before
  public void setUp() throws Exception {
    //make real validators do nothing because
    //we only want to test the aggregation here
    doNothing().when(stockEventVvmValidator).validate(any(StockEventDto.class));
    doNothing().when(approvedOrderableValidator).validate(any(StockEventDto.class));
    doNothing().when(sourceDestinationAssignmentValidator).validate(any(StockEventDto.class));
    doNothing().when(sourceDestinationGeoLeveLAffinityValidator).validate(any(StockEventDto.class));
    doNothing().when(mandatoryFieldsValidator).validate(any(StockEventDto.class));
    doNothing().when(freeTextValidator).validate(any(StockEventDto.class));
    doNothing().when(receiveIssueReasonValidator).validate(any(StockEventDto.class));
    doNothing().when(adjustmentReasonValidator).validate(any(StockEventDto.class));
    doNothing().when(quantityValidator).validate(any(StockEventDto.class));
    doNothing().when(lotValidator).validate(any(StockEventDto.class));
    doNothing().when(orderableLotDuplicationValidator).validate(any(StockEventDto.class));
    doNothing().when(reasonExistenceValidator).validate(any(StockEventDto.class));
    doNothing().when(physicalInventoryReasonsValidator).validate(any(StockEventDto.class));
    doNothing().when(unpackKitValidator).validate(any(StockEventDto.class));
    doNothing().when(duplicateTransactionValidator).validate(any(StockEventDto.class));
    when(extensionManager
        .getExtension(ExtensionPointId.ADJUSTMENT_REASON_POINT_ID, AdjustmentReasonValidator.class))
        .thenReturn(adjustmentReasonValidator);
    when(extensionManager
        .getExtension(ExtensionPointId.FREE_TEXT_POINT_ID, FreeTextValidator.class))
        .thenReturn(freeTextValidator);
    when(extensionManager
        .getExtension(ExtensionPointId.UNPACK_KIT_POINT_ID, UnpackKitValidator.class))
        .thenReturn(unpackKitValidator);
  }

  @Test
  public void shouldValidateWithAllImplementationsOfValidators() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();

    //when:
    stockEventValidationsService.validate(stockEventDto);

    //then:
    verify(stockEventVvmValidator, times(1)).validate(stockEventDto);
    verify(approvedOrderableValidator, times(1)).validate(stockEventDto);
    verify(sourceDestinationAssignmentValidator, times(1)).validate(stockEventDto);
    verify(sourceDestinationGeoLeveLAffinityValidator, times(1)).validate(stockEventDto);
    verify(mandatoryFieldsValidator, times(1)).validate(stockEventDto);
    verify(receiveIssueReasonValidator, times(1)).validate(stockEventDto);
    verify(quantityValidator, times(1)).validate(stockEventDto);
    verify(lotValidator, times(1)).validate(stockEventDto);
    verify(orderableLotDuplicationValidator, times(1)).validate(stockEventDto);
    verify(reasonExistenceValidator, times(1)).validate(stockEventDto);
    verify(physicalInventoryReasonsValidator, times(1)).validate(stockEventDto);
    verify(adjustmentReasonValidator, times(1)).validate(stockEventDto);
    verify(freeTextValidator, times(1)).validate(stockEventDto);
    verify(unpackKitValidator, times(1)).validate(stockEventDto);
    verify(duplicateTransactionValidator, times(1)).validate(stockEventDto);
  }

  @Test
  public void shouldNotRunNextValidatorIfPreviousValidatorFailed() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    doThrow(new ValidationMessageException(new Message("some error")))
        .when(approvedOrderableValidator).validate(stockEventDto);

    //when:
    try {
      stockEventValidationsService.validate(stockEventDto);
    } catch (ValidationMessageException ex) {
      //then:
      verify(approvedOrderableValidator, times(1)).validate(stockEventDto);
      verify(lotValidator, never()).validate(stockEventDto);
    }
  }

}