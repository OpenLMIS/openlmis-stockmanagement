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

package org.openlmis.stockmanagement.validator;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID;

import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.validators.DefaultAdjustmentReasonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdjustmentReasonValidatorIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private DefaultAdjustmentReasonValidator adjustmentReasonValidator;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    mockAuthentication();
  }

  @Test
  public void incorrectReasonTypeShouldNotPassWhenEventHasNoSourceAndDestination()
      throws Exception {
    //given
    StockCardLineItemReason reason = StockCardLineItemReason
        .builder()
        .reasonType(ReasonType.BALANCE_ADJUSTMENT)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .isFreeTextAllowed(true)
        .name("Balance Adjustment")
        .build();
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createNoSourceDestinationStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(reasonRepository.save(reason).getId());
    setContext(stockEventDto);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID + ": " + reason.getReasonType());

    //when
    adjustmentReasonValidator.validate(stockEventDto);
  }

  @Test
  public void incorrectReasonCategoryShouldNotPassWhenEventHasNoSourceAndDestination()
      throws Exception {
    //given

    StockCardLineItemReason reason = StockCardLineItemReason
        .builder()
        .reasonType(ReasonType.CREDIT)
        .reasonCategory(ReasonCategory.TRANSFER)
        .name("Credit Ad_hoc")
        .isFreeTextAllowed(false)
        .build();
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createNoSourceDestinationStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(reasonRepository.save(reason).getId());
    setContext(stockEventDto);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID + ": " + reason.getReasonCategory());

    //when
    adjustmentReasonValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotThrowExceptionIfEventHasNoSourceDestinationReasonId()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createNoSourceDestinationStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);
    setContext(stockEventDto);

    //when
    adjustmentReasonValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotThrowErrorForEventWithNoSourceDestinationAndReasonNotInDb()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createNoSourceDestinationStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(UUID.randomUUID());
    setContext(stockEventDto);

    //when
    adjustmentReasonValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotThrowErrorForEventWithNoReasonId() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);
    setContext(stockEventDto);

    //when
    adjustmentReasonValidator.validate(stockEventDto);
  }
}