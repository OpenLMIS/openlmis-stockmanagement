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

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_UNIT_OF_ORDERABLE_DOES_NOT_EXIST;

import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.UnitOfOrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.UnitOfOrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;

public class UnitOfOrderableValidatorTest {

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private UnitOfOrderableReferenceDataService unitOfOrderableReferenceDataService;

  @InjectMocks
  private UnitOfOrderableValidator unitOfOrderableValidator;

  @Test
  public void shouldPassWhenUnitOfOrderableExists() {
    //given
    UUID unitOfOrderableId = UUID.randomUUID();

    when(unitOfOrderableReferenceDataService.findOne(unitOfOrderableId))
        .thenReturn(mock(UnitOfOrderableDto.class));

    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDtoWithTwoLineItems();
    eventDto.getLineItems().forEach(
        lineItemDto -> lineItemDto.setUnitOfOrderableId(unitOfOrderableId)
    );

    //when
    unitOfOrderableValidator.validate(eventDto);

    //then
    // no exception - ok
  }

  @Test
  public void shouldNotPassWhenUnitOfOrderableDoesNotExist() {
    //given
    UUID unitOfOrderableId = UUID.randomUUID();

    when(unitOfOrderableReferenceDataService.findOne(unitOfOrderableId))
        .thenReturn(null);

    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDtoWithTwoLineItems();
    eventDto.getLineItems().forEach(
        lineItemDto -> lineItemDto.setUnitOfOrderableId(unitOfOrderableId)
    );

    //expect: exception
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        containsString(ERROR_LINE_ITEM_UNIT_OF_ORDERABLE_DOES_NOT_EXIST)
    );

    //when-then
    unitOfOrderableValidator.validate(eventDto);
  }
}
