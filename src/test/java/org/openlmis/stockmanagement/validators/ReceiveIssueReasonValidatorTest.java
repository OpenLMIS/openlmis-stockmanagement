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

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.ADJUSTMENT;
import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.TRANSFER;
import static org.openlmis.stockmanagement.domain.reason.ReasonType.CREDIT;
import static org.openlmis.stockmanagement.domain.reason.ReasonType.DEBIT;
import static org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason.builder;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ISSUE_REASON_TYPE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveIssueReasonValidatorTest extends BaseValidatorTest  {

  @Rule
  public ExpectedException expectedEx = none();

  @InjectMocks
  private ReceiveIssueReasonValidator receiveIssueReasonValidator;

  private StockCardLineItemReason creditAdhocReason;
  private StockCardLineItemReason debitAdhocReason;
  private StockCardLineItemReason creditNonAdhocReason;
  private StockCardLineItemReason debitNonAdhocReason;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    creditAdhocReason = builder().reasonType(CREDIT).reasonCategory(TRANSFER).build();
    debitAdhocReason = builder().reasonType(DEBIT).reasonCategory(TRANSFER).build();
    creditNonAdhocReason = builder().reasonType(CREDIT).reasonCategory(ADJUSTMENT).build();
    debitNonAdhocReason = builder().reasonType(DEBIT).reasonCategory(ADJUSTMENT).build();
  }

  @Test
  public void event_that_has_source_but_no_reason_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    stockEventDto.getLineItems().get(0).setReasonId(null);
    setContext(stockEventDto);

    //when
    receiveIssueReasonValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_destination_but_no_reason_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setReasonId(null);
    setContext(stockEventDto);

    //when
    receiveIssueReasonValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_source_and_a_reason_of_credit_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setDestinationId(null);

    UUID reasonId = UUID.randomUUID();
    stockEventDto.getLineItems().get(0).setReasonId(reasonId);
    setContext(stockEventDto);

    when(reasonRepository.findByIdIn(singleton(reasonId)))
        .thenReturn(singletonList(creditAdhocReason));

    creditAdhocReason.setId(reasonId);

    //when
    receiveIssueReasonValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_destination_and_a_reason_of_debit_should_pass() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setSourceId(null);
    //the following is a debit reason
    UUID reasonId = UUID.randomUUID();
    stockEventDto.getLineItems().get(0).setReasonId(reasonId);
    setContext(stockEventDto);

    when(reasonRepository.findByIdIn(singleton(reasonId)))
        .thenReturn(singletonList(debitAdhocReason));

    debitAdhocReason.setId(reasonId);

    //when
    receiveIssueReasonValidator.validate(stockEventDto);

    //then: no error exception
  }

  @Test
  public void event_that_has_source_and_a_reason_of_debit_should_not_pass() throws Exception {
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    stockEventDto.getLineItems().get(0).setReasonId(UUID.randomUUID());
    testErrorCase(ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID, stockEventDto, debitAdhocReason);
  }

  @Test
  public void event_that_has_destination_and_a_reason_of_credit_should_not_pass() throws Exception {
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setReasonId(UUID.randomUUID());

    testErrorCase(ERROR_EVENT_ISSUE_REASON_TYPE_INVALID, stockEventDto, creditAdhocReason);
  }

  @Test
  public void event_that_has_source_and_a_reason_of_non_adhoc_category_should_not_pass()
      throws Exception {
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    stockEventDto.getLineItems().get(0).setReasonId(UUID.randomUUID());

    testErrorCase(ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID, stockEventDto, creditNonAdhocReason);
  }

  @Test
  public void event_that_has_destination_and_a_reason_of_non_adhoc_category_should_not_pass()
      throws Exception {
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setDestinationId(UUID.randomUUID());
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setReasonId(UUID.randomUUID());

    testErrorCase(ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID, stockEventDto, debitNonAdhocReason);
  }

  private void testErrorCase(String errorKey, StockEventDto stockEventDto,
                             StockCardLineItemReason mockedReason) {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(errorKey);

    //given
    UUID reasonId = stockEventDto.getLineItems().get(0).getReasonId();
    mockedReason.setId(reasonId);

    setContext(stockEventDto);

    when(reasonRepository.findByIdIn(singleton(reasonId)))
        .thenReturn(singletonList(mockedReason));

    //when
    receiveIssueReasonValidator.validate(stockEventDto);
  }
}