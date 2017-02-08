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
  public void should_save_stock_card_line_items_and_create_stock_card_for_first_movement()
          throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setStockCardId(null);//first movement, no card id
    StockEvent savedEvent = stockEventsRepository.save(stockEventDto.toEvent(userId));

    //when
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId(), userId);

    //then
    List<StockCardLineItem> savedLineItems =
            stream(stockCardLineItemsRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());

    StockCardLineItem firstLineItem = savedLineItems.get(0);

    assertThat(firstLineItem.getUserId(), is(userId));
    assertThat(firstLineItem.getStockCard().getOriginEvent().getId(), is(savedEvent.getId()));
    assertThat(firstLineItem.getStockCard().getFacilityId(), is(savedEvent.getFacilityId()));
    assertThat(firstLineItem.getStockCard().getProgramId(), is(savedEvent.getProgramId()));
    assertThat(firstLineItem.getStockCard().getOrderableId(), is(savedEvent.getOrderableId()));
  }

  @Test
  public void should_save_stock_card_line_items_with_existing_stock_card() throws Exception {
    //given
    //1. there is an existing event that caused a stock card to exist
    StockEventDto cardEventDto = createStockEventDto();
    StockEvent savedCardEvent = stockEventsRepository
            .save(cardEventDto.toEvent(UUID.randomUUID()));
    StockCard existingCard = stockCardRepository
            .save(StockCard.createStockCardFrom(cardEventDto, savedCardEvent.getId()));

    //2. and there is a new event saved
    UUID userId = UUID.randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setStockCardId(existingCard.getId());
    stockEventDto.setProgramId(cardEventDto.getProgramId());
    stockEventDto.setFacilityId(cardEventDto.getFacilityId());
    stockEventDto.setOrderableId(cardEventDto.getOrderableId());

    StockEvent savedEvent = stockEventsRepository.save(stockEventDto.toEvent(userId));

    //when
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId(), userId);

    //then
    List<StockCardLineItem> savedLineItems =
            stream(stockCardLineItemsRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());
    StockCardLineItem latestLineItem = savedLineItems.get(savedLineItems.size() - 1);

    assertThat(latestLineItem.getUserId(), is(userId));
    assertThat(latestLineItem.getStockCard().getId(), is(existingCard.getId()));
  }

  @Test
  public void should_save_line_items_with_program_facility_orderable_for_non_first_movement()
          throws Exception {
    //given
    //1. there is an existing event that caused a stock card to exist
    StockEventDto cardEventDto = createStockEventDto();
    StockEvent savedCardEvent = stockEventsRepository
            .save(cardEventDto.toEvent(UUID.randomUUID()));
    final StockCard existingCard = stockCardRepository
            .save(StockCard.createStockCardFrom(cardEventDto, savedCardEvent.getId()));

    //2. and there is a new event saved
    UUID userId = UUID.randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setProgramId(cardEventDto.getProgramId());
    stockEventDto.setFacilityId(cardEventDto.getFacilityId());
    stockEventDto.setOrderableId(cardEventDto.getOrderableId());
    stockEventDto.setStockCardId(null);
    StockEvent savedEvent = stockEventsRepository.save(stockEventDto.toEvent(userId));

    //when
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId(), userId);

    //then
    List<StockCardLineItem> savedLineItems =
            stream(stockCardLineItemsRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());
    StockCardLineItem latestLineItem = savedLineItems.get(savedLineItems.size() - 1);

    assertThat(latestLineItem.getUserId(), is(userId));
    assertThat(latestLineItem.getStockCard().getId(), is(existingCard.getId()));
  }
}