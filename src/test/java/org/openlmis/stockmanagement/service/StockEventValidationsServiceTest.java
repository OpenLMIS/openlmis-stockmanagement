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
import org.openlmis.stockmanagement.dto.StockEventDto2;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.utils.Message;
import org.openlmis.stockmanagement.validators.AdjustmentReasonValidator2;
import org.openlmis.stockmanagement.validators.ApprovedOrderableValidator2;
import org.openlmis.stockmanagement.validators.FreeTextValidator2;
import org.openlmis.stockmanagement.validators.MandatoryFieldsValidator2;
import org.openlmis.stockmanagement.validators.QuantityValidator2;
import org.openlmis.stockmanagement.validators.ReasonAssignmentValidator2;
import org.openlmis.stockmanagement.validators.ReceiveIssueReasonValidator2;
import org.openlmis.stockmanagement.validators.SourceDestinationAssignmentValidator2;
import org.openlmis.stockmanagement.validators.StockEventValidator2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventValidationsServiceTest {

  @Autowired
  private StockEventValidationsService2 stockEventValidationsService;

  @MockBean(name = "v1")
  private StockEventValidator2 validator1;

  @MockBean(name = "v2")
  private StockEventValidator2 validator2;

  @MockBean
  private ApprovedOrderableValidator2 approvedOrderableValidator;

  @MockBean
  private SourceDestinationAssignmentValidator2 sourceDestinationAssignmentValidator;

  @MockBean
  private MandatoryFieldsValidator2 mandatoryFieldsValidator;

  @MockBean
  private ReceiveIssueReasonValidator2 receiveIssueReasonValidator;

  @MockBean
  private AdjustmentReasonValidator2 adjustmentReasonValidator;

  @MockBean
  private FreeTextValidator2 freeTextValidator;

  @MockBean
  private QuantityValidator2 quantityValidator;

  @MockBean
  private ReasonAssignmentValidator2 reasonAssignmentValidator;

  @Before
  public void setUp() throws Exception {
    //make real validators do nothing because
    //we only want to test the aggregation here
    doNothing().when(approvedOrderableValidator).validate(any(StockEventDto2.class));
    doNothing().when(sourceDestinationAssignmentValidator).validate(any(StockEventDto2.class));
    doNothing().when(mandatoryFieldsValidator).validate(any(StockEventDto2.class));
    doNothing().when(freeTextValidator).validate(any(StockEventDto2.class));
    doNothing().when(receiveIssueReasonValidator).validate(any(StockEventDto2.class));
    doNothing().when(adjustmentReasonValidator).validate(any(StockEventDto2.class));
    doNothing().when(quantityValidator).validate(any(StockEventDto2.class));
    doNothing().when(reasonAssignmentValidator).validate(any(StockEventDto2.class));
  }

  @Test
  public void should_validate_with_all_implementations_of_validators() throws Exception {
    //given
    StockEventDto2 stockEventDto = StockEventDtoBuilder.createStockEventDto2();

    //when:
    stockEventValidationsService.validate(stockEventDto);

    //then:
    verify(validator1, times(1)).validate(stockEventDto);
    verify(validator2, times(1)).validate(stockEventDto);
  }

  @Test
  public void should_not_run_next_validator_if_previous_validator_failed() throws Exception {
    //given
    StockEventDto2 stockEventDto = StockEventDtoBuilder.createStockEventDto2();
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