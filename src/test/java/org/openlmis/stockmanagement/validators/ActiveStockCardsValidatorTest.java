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
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createNoSourceDestinationStockEventDto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStockCardsValidatorTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private StockCardRepository stockCardRepository;

  @InjectMocks
  private ActiveStockCardsValidator activeStockCardsValidator;

  @Test
  public void should_throw_exception_if_existing_card_orderable_not_covered() throws Exception {
    expectedEx.expectMessage(ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD);

    //given
    StockEventDto stockEventDto = createNoSourceDestinationStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);

    when(stockCardRepository
        .getIdentitiesBy(stockEventDto.getProgramId(), stockEventDto.getFacilityId()))
        .thenReturn(singletonList(new OrderableLotIdentity(randomUUID(), randomUUID())));

    //when
    activeStockCardsValidator.validate(stockEventDto);
  }

  @Test
  public void should_throw_exception_if_existing_card_lot_not_covered() throws Exception {
    expectedEx.expectMessage(ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD);

    //given
    StockEventDto stockEventDto = createNoSourceDestinationStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);
    stockEventDto.getLineItems().get(0).setLotId(randomUUID());

    when(stockCardRepository
        .getIdentitiesBy(stockEventDto.getProgramId(), stockEventDto.getFacilityId()))
        .thenReturn(singletonList(new OrderableLotIdentity(randomUUID(), randomUUID())));

    //when
    activeStockCardsValidator.validate(stockEventDto);
  }
}