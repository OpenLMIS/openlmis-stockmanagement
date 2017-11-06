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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockCardServiceTest {

  @Mock
  private StockCardRepository cardRepository;

  @InjectMocks
  private StockCardService stockCardService;

  @Captor
  private ArgumentCaptor<List<StockCard>> cardCaptor;

  @Test
  public void shouldNotDuplicateCardsForOrderableLots() throws Exception {
    StockEventDto event = StockEventDtoBuilder.createStockEventDtoWithTwoLineItems();
    event.setContext(mock(StockEventProcessContext.class));

    UUID savedEventId = UUID.randomUUID();

    stockCardService.saveFromEvent(event, savedEventId);

    verify(cardRepository).save(cardCaptor.capture());

    List<StockCard> saved = cardCaptor.getValue();

    assertThat(saved, hasSize(1));

    StockCard card = saved.get(0);

    assertThat(card.getFacilityId(), equalTo(event.getFacilityId()));
    assertThat(card.getProgramId(), equalTo(event.getProgramId()));

    assertThat(card.getOrderableId(), equalTo(event.getLineItems().get(0).getOrderableId()));
    assertThat(card.getLotId(), equalTo(event.getLineItems().get(0).getLotId()));

    assertThat(card.getOrderableId(), equalTo(event.getLineItems().get(1).getOrderableId()));
    assertThat(card.getLotId(), equalTo(event.getLineItems().get(1).getLotId()));

    assertThat(card.getLineItems(), hasSize(2));
  }
}