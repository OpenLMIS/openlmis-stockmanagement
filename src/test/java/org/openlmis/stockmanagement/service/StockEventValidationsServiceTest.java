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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.utils.Message;
import org.openlmis.stockmanagement.validators.AdjustmentReasonValidator;
import org.openlmis.stockmanagement.validators.ApprovedOrderableValidator;
import org.openlmis.stockmanagement.validators.DiscrepancyReasonsValidator;
import org.openlmis.stockmanagement.validators.FreeTextValidator;
import org.openlmis.stockmanagement.validators.LotValidator;
import org.openlmis.stockmanagement.validators.MandatoryFieldsValidator;
import org.openlmis.stockmanagement.validators.OrderableLotDuplicationValidator;
import org.openlmis.stockmanagement.validators.QuantityValidator;
import org.openlmis.stockmanagement.validators.ReasonExistenceValidator;
import org.openlmis.stockmanagement.validators.ReceiveIssueReasonValidator;
import org.openlmis.stockmanagement.validators.SourceDestinationAssignmentValidator;
import org.openlmis.stockmanagement.validators.StockEventVvmValidator;
import org.openlmis.stockmanagement.validators.StockEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventValidationsServiceTest {

  @Autowired
  private StockEventValidationsService stockEventValidationsService;

  @MockBean(name = "v1")
  private StockEventValidator validator1;

  @MockBean(name = "v2")
  private StockEventValidator validator2;

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
  private AdjustmentReasonValidator adjustmentReasonValidator;

  @MockBean
  private FreeTextValidator freeTextValidator;

  @MockBean
  private QuantityValidator quantityValidator;

  @MockBean
  private LotValidator lotValidator;

  @MockBean
  private ReasonExistenceValidator reasonExistenceValidator;

  @MockBean
  private OrderableLotDuplicationValidator orderableLotDuplicationValidator;

  @MockBean
  private DiscrepancyReasonsValidator discrepancyReasonsValidator;

  @Before
  public void setUp() throws Exception {
    //make real validators do nothing because
    //we only want to test the aggregation here
    doNothing().when(stockEventVvmValidator).validate(any(StockEventDto.class));
    doNothing().when(approvedOrderableValidator).validate(any(StockEventDto.class));
    doNothing().when(sourceDestinationAssignmentValidator).validate(any(StockEventDto.class));
    doNothing().when(mandatoryFieldsValidator).validate(any(StockEventDto.class));
    doNothing().when(freeTextValidator).validate(any(StockEventDto.class));
    doNothing().when(receiveIssueReasonValidator).validate(any(StockEventDto.class));
    doNothing().when(adjustmentReasonValidator).validate(any(StockEventDto.class));
    doNothing().when(quantityValidator).validate(any(StockEventDto.class));
    doNothing().when(lotValidator).validate(any(StockEventDto.class));
    doNothing().when(orderableLotDuplicationValidator).validate(any(StockEventDto.class));
    doNothing().when(reasonExistenceValidator).validate(any(StockEventDto.class));
    doNothing().when(discrepancyReasonsValidator).validate(any(StockEventDto.class));
  }

  @Test
  public void should_validate_with_all_implementations_of_validators() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();

    //when:
    stockEventValidationsService.validate(stockEventDto);

    //then:
    verify(validator1, times(1)).validate(stockEventDto);
    verify(validator2, times(1)).validate(stockEventDto);
  }

  @Test
  public void should_not_run_next_validator_if_previous_validator_failed() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    doThrow(new ValidationMessageException(new Message("some error")))
        .when(validator1).validate(stockEventDto);

    //when:
    try {
      stockEventValidationsService.validate(stockEventDto);
    } catch (ValidationMessageException ex) {
      //then:
      verify(validator1, times(1)).validate(stockEventDto);
      verify(validator2, never()).validate(stockEventDto);
    }
  }

}