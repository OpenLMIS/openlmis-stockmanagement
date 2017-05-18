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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_NOT_EXIST;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.util.HashMap;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class LotValidatorTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @InjectMocks
  private LotValidator lotValidator;

  @Test
  public void should_fail_if_lot_does_not_exist() throws Exception {
    //expect
    expectedEx.expectMessage(ERROR_EVENT_LOT_NOT_EXIST);

    //given
    StockEventDto stockEventDto = createStockEventDto();

    UUID lotId = randomUUID();
    stockEventDto.getLineItems().get(0).setLotId(lotId);

    HashMap<UUID, LotDto> lots = new HashMap<>();
    StockEventProcessContext context = new StockEventProcessContext();
    context.setLots(lots);
    stockEventDto.setContext(context);

    //when
    lotValidator.validate(stockEventDto);
  }

  @Test
  public void should_fail_if_lot_does_not_match_orderable() throws Exception {
    //expect
    expectedEx.expectMessage(ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH);

    //given
    StockEventDto stockEventDto = createStockEventDto();

    UUID lotId = randomUUID();
    stockEventDto.getLineItems().get(0).setLotId(lotId);

    LotDto lotDto = new LotDto();
    lotDto.setTradeItemId(UUID.randomUUID());

    HashMap<UUID, LotDto> lots = new HashMap<>();
    lots.put(lotId, lotDto);

    StockEventProcessContext context = new StockEventProcessContext();
    context.setLots(lots);
    context.setAllApprovedProducts(
        singletonList(OrderableDto.builder()
            .id(stockEventDto.getLineItems().get(0).getOrderableId())
            .identifiers(emptyMap())
            .build()));

    stockEventDto.setContext(context);

    //when
    lotValidator.validate(stockEventDto);
  }
}