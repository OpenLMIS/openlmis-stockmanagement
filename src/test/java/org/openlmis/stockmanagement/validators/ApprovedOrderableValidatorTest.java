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

import static java.util.Collections.singletonList;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.util.LazyList;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

@SuppressWarnings("PMD.UnusedPrivateField")
@RunWith(MockitoJUnitRunner.class)
public class ApprovedOrderableValidatorTest extends BaseValidatorTest  {

  @InjectMocks
  private ApprovedOrderableValidator approvedOrderableValidator;

  @Test(expected = ValidationMessageException.class)
  public void stockEventWithOrderableIdNotInApprovedListShouldNotPassValidation()
      throws Exception {
    //given:
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();

    OrderableDto orderableDto = new OrderableDto();
    orderableDto.setId(UUID.randomUUID());

    StockEventProcessContext context = new StockEventProcessContext();
    context.setAllApprovedProducts(new LazyList<>(() -> singletonList(orderableDto)));

    stockEventDto.setContext(context);

    //when:
    approvedOrderableValidator.validate(stockEventDto);
  }

  @Test
  public void stockEventWithOrderableIdInApprovedListShouldPass()
      throws Exception {
    //given:
    String orderableIdString = "d8290082-f9fa-4a37-aefb-a3d76ff805a8";
    UUID orderableId = UUID.fromString(orderableIdString);

    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setOrderableId(orderableId);

    OrderableDto orderableDto = new OrderableDto();
    orderableDto.setId(UUID.fromString(orderableIdString));

    StockEventProcessContext context = new StockEventProcessContext();
    context.setAllApprovedProducts(new LazyList<>(() -> singletonList(orderableDto)));

    stockEventDto.setContext(context);

    //when:
    approvedOrderableValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotThrowValidationExceptionIfEventHasNoProgramAndFacilityId()
      throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.setProgramId(null);

    //when
    approvedOrderableValidator.validate(stockEventDto);

    //given
    stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.setFacilityId(null);

    //when
    approvedOrderableValidator.validate(stockEventDto);
  }

}
