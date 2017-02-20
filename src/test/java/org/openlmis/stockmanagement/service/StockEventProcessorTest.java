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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockEventProcessorTest extends BaseTest {

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private StockEventValidationsService stockEventValidationsService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Before
  public void setUp() throws Exception {
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto();
    userDto.setId(userId);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
  }

  @After
  public void tearDown() throws Exception {
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
      assertEventAndCardAndLineItemTableSize(0);
      return;
    }

    Assert.fail();
  }

  @Test
  public void should_save_event_and_line_items_when_validation_service_passes() throws Exception {
    //when
    assertEventAndCardAndLineItemTableSize(0);

    StockEventDto stockEventDto = createStockEventDto();
    stockEventProcessor.process(stockEventDto);

    //then
    assertEventAndCardAndLineItemTableSize(1);
  }

  private void assertEventAndCardAndLineItemTableSize(long size) {
    Iterable<StockCard> allCards = stockCardRepository.findAll();
    long lineItemsCount = stream(allCards.spliterator(), false)
            .flatMap(card -> card.getLineItems().stream()).count();

    assertThat(allCards.spliterator().getExactSizeIfKnown(), is(size));
    assertThat(lineItemsCount, is(size));
    assertThat(stockEventsRepository.count(), is(size));
  }
}
