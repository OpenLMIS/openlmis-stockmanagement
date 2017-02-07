package org.openlmis.stockmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemsRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createFrom;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardServiceTest extends BaseTest {

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockCardLineItemsRepository stockCardLineItemsRepository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Test
  public void should_save_stock_card_line_items_with_existing_stock_card() throws Exception {
    //given
    //there is an existing stock card
    StockCard existingCard = stockCardRepository.save(new StockCard());

    //and there is a new event saved
    UUID userId = UUID.randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    StockEvent savedEvent = stockEventsRepository.save(stockEventDto.toEvent(userId));
    stockEventDto.setStockCardId(existingCard.getId());

    //line items are created from the new event
    List<StockCardLineItem> stockCardLineItems =
            createFrom(stockEventDto, savedEvent.getId(), userId);

    //when
    stockCardService.save(stockCardLineItems);

    //then
    List<StockCardLineItem> savedLineItems =
            stream(stockCardLineItemsRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());

    assertThat(savedLineItems.get(0).getUserId(), is(userId));
    assertThat(savedLineItems.get(0).getOriginEvent().getId(), is(savedEvent.getId()));
    assertThat(savedLineItems.get(0).getStockCard().getId(), is(existingCard.getId()));
  }
}