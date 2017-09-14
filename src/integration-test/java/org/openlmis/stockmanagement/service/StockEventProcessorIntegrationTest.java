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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventLineItem;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.notifier.StockoutNotifier;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventProcessorIntegrationTest extends BaseIntegrationTest {

  @MockBean
  private StockEventValidationsService stockEventValidationsService;

  @MockBean
  private StockEventProcessContextBuilder contextBuilder;

  @MockBean
  private StockoutNotifier stockoutNotifier;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockCardLineItemRepository lineItemRepository;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private OAuth2Authentication authentication;

  private long cardSize;
  private long eventSize;
  private long lineItemSize;

  @Before
  public void setUp() throws Exception {
    cardSize = stockCardRepository.count();
    eventSize = stockEventsRepository.count();
    lineItemSize = lineItemRepository.count();

    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto();
    userDto.setId(userId);

    StockEventProcessContext eventProcessContext = new StockEventProcessContext();
    eventProcessContext.setCurrentUser(userDto);

    when(contextBuilder.buildContext(any(StockEventDto.class)))
        .thenReturn(eventProcessContext);

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(false);
  }

  @After
  public void tearDown() throws Exception {
    physicalInventoriesRepository.deleteAll();
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
  }

  @Test
  public void should_not_save_events_if_anything_goes_wrong_in_validations_service()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();

    Mockito.doThrow(new RuntimeException("something wrong from validations service"))
        .when(stockEventValidationsService)
        .validate(stockEventDto);

    //when
    try {
      stockEventProcessor.process(stockEventDto);
    } catch (RuntimeException ex) {
      //then
      assertSize(cardSize, eventSize, lineItemSize);
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_save_event_and_line_items_when_validation_service_passes() throws Exception {
    StockEventDto stockEventDto = createStockEventDto();

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    assertSize(cardSize + 1, eventSize + 1, lineItemSize + 1);
  }

  @Test
  public void should_persist_physical_inventory_when_event_is_about_physical_inventory()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(null);
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(null);

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    assertSize(cardSize + 1, eventSize + 1, lineItemSize + 1);
    assertThat(physicalInventoriesRepository.count(), is(1L));
  }

  @Test
  public void shouldCallStockoutNotifierWhenStockOnHandIsZero() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    StockEventLineItem firstLineItem = stockEventDto.getLineItems().get(0);
    firstLineItem.setQuantity(0);

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    ArgumentCaptor<StockCard> captor = ArgumentCaptor.forClass(StockCard.class);
    verify(stockoutNotifier).notifyStockEditors(captor.capture());

    StockCard createdCard = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableIdAndLotId(
        stockEventDto.getProgramId(), stockEventDto.getFacilityId(),
        firstLineItem.getOrderableId(), firstLineItem.getLotId());
    assertEquals(createdCard.getId(), captor.getValue().getId());
    assertEquals(stockEventDto.getProgramId(), captor.getValue().getProgramId());
    assertEquals(stockEventDto.getFacilityId(), captor.getValue().getFacilityId());
    assertEquals(firstLineItem.getOrderableId(), captor.getValue().getOrderableId());
    assertEquals(firstLineItem.getLotId(), captor.getValue().getLotId());
  }

  @Test
  public void shouldCallStockoutNotifierForEveryCard() throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    StockEventLineItem firstLineItem = stockEventDto.getLineItems().get(0);
    firstLineItem.setQuantity(0);

    StockEventLineItem secondLineItem = createStockEventLineItem();
    secondLineItem.setQuantity(0);
    stockEventDto.setLineItems(Arrays.asList(firstLineItem, secondLineItem));

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    verify(stockoutNotifier, times(2)).notifyStockEditors(any(StockCard.class));
  }

  private void assertSize(long cardSize, long eventSize, long lineItemSize) {
    assertThat(stockCardRepository.count(), is(cardSize));
    assertThat(stockEventsRepository.count(), is(eventSize));
    assertThat(lineItemRepository.count(), is(lineItemSize));
  }
}
