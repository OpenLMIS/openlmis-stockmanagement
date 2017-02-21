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

package org.openlmis.stockmanagement.validators;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;

import java.util.UUID;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.domain.adjustment.ReasonCategory.ADJUSTMENT;
import static org.openlmis.stockmanagement.domain.adjustment.ReasonCategory.AD_HOC;
import static org.openlmis.stockmanagement.domain.adjustment.ReasonType.CREDIT;
import static org.openlmis.stockmanagement.domain.adjustment.ReasonType.DEBIT;
import static org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason.builder;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ISSUE_REASON_TYPE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveAndIssueValidatorTest {

  @Rule
  public ExpectedException expectedEx = none();

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @InjectMocks
  private ReceiveAndIssueValidator receiveAndIssueValidator;

  private StockCardLineItemReason creditAdhocReason;
  private StockCardLineItemReason debitAdhocReason;
  private StockCardLineItemReason creditNonAdhocReason;
  private StockCardLineItemReason debitNonAdhocReason;

  @Before
  public void setUp() throws Exception {
    creditAdhocReason = builder().reasonType(CREDIT).reasonCategory(AD_HOC).build();
    debitAdhocReason = builder().reasonType(DEBIT).reasonCategory(AD_HOC).build();
    creditNonAdhocReason = builder().reasonType(CREDIT).reasonCategory(ADJUSTMENT).build();
    debitNonAdhocReason = builder().reasonType(DEBIT).reasonCategory(ADJUSTMENT).build();
  }

  @Test
  public void event_that_has_source_but_no_reason_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setSourceId(UUID.randomUUID());
    stockEventDto.setDestinationId(null);
    stockEventDto.setReasonId(null);

    //when
    receiveAndIssueValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_destination_but_no_reason_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setDestinationId(UUID.randomUUID());
    stockEventDto.setSourceId(null);
    stockEventDto.setReasonId(null);

    //when
    receiveAndIssueValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_source_and_a_reason_of_credit_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setSourceId(UUID.randomUUID());
    stockEventDto.setDestinationId(null);
    stockEventDto.setReasonId(UUID.randomUUID());

    when(reasonRepository.findOne(stockEventDto.getReasonId())).thenReturn(creditAdhocReason);

    //when
    receiveAndIssueValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_destination_and_a_reason_of_debit_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setDestinationId(UUID.randomUUID());
    stockEventDto.setSourceId(null);
    //the following is a debit reason
    stockEventDto.setReasonId(UUID.randomUUID());

    when(reasonRepository.findOne(stockEventDto.getReasonId())).thenReturn(debitAdhocReason);

    //when
    receiveAndIssueValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_source_and_a_reason_of_debit_should_not_pass() throws Exception {
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setSourceId(UUID.randomUUID());
    stockEventDto.setDestinationId(null);
    stockEventDto.setReasonId(UUID.randomUUID());
    testErrorCase(ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID, stockEventDto, debitAdhocReason);
  }

  @Test
  public void event_that_has_destination_and_a_reason_of_credit_should_not_pass() throws Exception {
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setDestinationId(UUID.randomUUID());
    stockEventDto.setSourceId(null);
    stockEventDto.setReasonId(UUID.randomUUID());

    testErrorCase(ERROR_EVENT_ISSUE_REASON_TYPE_INVALID, stockEventDto, creditAdhocReason);
  }

  @Test
  public void event_that_has_source_and_a_reason_of_non_adhoc_category_should_not_pass()
          throws Exception {
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setSourceId(UUID.randomUUID());
    stockEventDto.setDestinationId(null);
    stockEventDto.setReasonId(UUID.randomUUID());

    testErrorCase(ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID, stockEventDto, creditNonAdhocReason);
  }

  @Test
  public void event_that_has_destination_and_a_reason_of_non_adhoc_category_should_not_pass()
          throws Exception {
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setDestinationId(UUID.randomUUID());
    stockEventDto.setSourceId(null);
    stockEventDto.setReasonId(UUID.randomUUID());

    testErrorCase(ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID, stockEventDto, debitNonAdhocReason);
  }

  private void testErrorCase(String errorKey, StockEventDto stockEventDto,
                             StockCardLineItemReason mockedReason) {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(errorKey);

    //given

    when(reasonRepository.findOne(stockEventDto.getReasonId())).thenReturn(mockedReason);

    //when
    receiveAndIssueValidator.validate(stockEventDto);
  }
}