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

import static org.junit.rules.ExpectedException.none;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_REASON_NOT_EXIST;

import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ReasonExistenceValidatorTest extends BaseValidatorTest  {
  @Rule
  public ExpectedException expectedEx = none();

  @InjectMocks
  private ReasonExistenceValidator reasonExistenceValidator;

  @Test
  public void shouldNotThrowErrorIfEventHasNoReasonId() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);
    setContext(stockEventDto);

    //when
    reasonExistenceValidator.validate(stockEventDto);

    //then: no exception
  }

  @Test
  public void shouldThrowErrorIfEventReasonIdNotFoundInDb()
      throws Exception {
    //expect
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_EVENT_REASON_NOT_EXIST);

    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    UUID reasonId = UUID.randomUUID();
    stockEventDto.getLineItems().get(0).setReasonId(reasonId);
    setContext(stockEventDto);

    //when
    reasonExistenceValidator.validate(stockEventDto);
  }

}