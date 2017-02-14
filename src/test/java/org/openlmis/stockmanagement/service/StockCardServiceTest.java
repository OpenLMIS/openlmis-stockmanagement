package org.openlmis.stockmanagement.service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardServiceTest extends BaseTest {

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @MockBean
  private FacilityReferenceDataService facilityReferenceDataService;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @After
  public void tearDown() throws Exception {
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
  }

  @Test
  public void should_save_stock_card_line_items_and_create_stock_card_for_first_movement()
          throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    StockEvent savedEvent = stockEventsRepository.save(stockEventDto.toEvent(userId));

    //when
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId(), userId);

    //then
    StockCard savedCard = stockCardRepository.findByOriginEvent(savedEvent);
    StockCardLineItem firstLineItem = savedCard.getLineItems().get(0);

    assertThat(firstLineItem.getUserId(), is(userId));
    assertThat(firstLineItem.getSource().isRefDataFacility(), is(true));
    assertThat(firstLineItem.getDestination().isRefDataFacility(), is(false));

    assertThat(firstLineItem.getStockCard().getOriginEvent().getId(), is(savedEvent.getId()));
    assertThat(firstLineItem.getStockCard().getFacilityId(), is(savedEvent.getFacilityId()));
    assertThat(firstLineItem.getStockCard().getProgramId(), is(savedEvent.getProgramId()));
    assertThat(firstLineItem.getStockCard().getOrderableId(), is(savedEvent.getOrderableId()));
  }

  @Test
  public void should_save_line_items_with_program_facility_orderable_for_non_first_movement()
          throws Exception {
    //given
    //1. there is an existing event that caused a stock card to exist
    StockEventDto existingEventDto = createStockEventDto();
    StockEvent existingEvent = stockEventsRepository
            .save(existingEventDto.toEvent(UUID.randomUUID()));
    final StockCard existingCard = stockCardRepository
            .save(StockCard.createStockCardFrom(existingEventDto, existingEvent.getId()));

    //2. and there is a new event coming
    UUID userId = UUID.randomUUID();
    StockEventDto newEventDto = createStockEventDto();
    newEventDto.setProgramId(existingEventDto.getProgramId());
    newEventDto.setFacilityId(existingEventDto.getFacilityId());
    newEventDto.setOrderableId(existingEventDto.getOrderableId());
    StockEvent savedNewEvent = stockEventsRepository.save(newEventDto.toEvent(userId));

    //when
    long cardAmountBeforeSave = stockCardRepository.count();
    stockCardService.saveFromEvent(newEventDto, savedNewEvent.getId(), userId);
    long cardAmountAfterSave = stockCardRepository.count();

    //then
    StockCard savedCard = stockCardRepository.findByOriginEvent(existingEvent);
    List<StockCardLineItem> lineItems = savedCard.getLineItems();
    StockCardLineItem latestLineItem = lineItems.get(lineItems.size() - 1);

    assertThat(cardAmountAfterSave, is(cardAmountBeforeSave));
    assertThat(latestLineItem.getUserId(), is(userId));
    assertThat(latestLineItem.getStockCard().getId(), is(existingCard.getId()));
  }

  @Test
  public void should_get_refdata_and_convert_organizations_when_find_stock_card()
          throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    StockEventDto stockEventDto = createStockEventDto();

    //1. mock ref data service
    FacilityDto cardFacility = new FacilityDto();
    FacilityDto sourceFacility = new FacilityDto();
    ProgramDto programDto = new ProgramDto();
    OrderableDto orderableDto = new OrderableDto();

    when(facilityReferenceDataService.findOne(stockEventDto.getFacilityId()))
            .thenReturn(cardFacility);
    when(facilityReferenceDataService.findOne(fromString("e6799d64-d10d-4011-b8c2-0e4d4a3f65ce")))
            .thenReturn(sourceFacility);

    when(programReferenceDataService.findOne(stockEventDto.getProgramId()))
            .thenReturn(programDto);
    when(orderableReferenceDataService.findOne(stockEventDto.getOrderableId()))
            .thenReturn(orderableDto);

    //2. there is an existing stock card with line items
    StockEvent savedEvent = stockEventsRepository.save(stockEventDto.toEvent(userId));
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId(), userId);
    UUID savedCardId = stockCardRepository.findByOriginEvent(savedEvent).getId();

    //when
    StockCardDto foundCardDto = stockCardService.findStockCardById(savedCardId);

    //then
    assertThat(foundCardDto.getFacility(), is(cardFacility));
    assertThat(foundCardDto.getProgram(), is(programDto));
    assertThat(foundCardDto.getOrderable(), is(orderableDto));

    StockCardLineItemDto lineItemDto = foundCardDto.getLineItems().get(0);
    assertThat(lineItemDto.getSource(), is(sourceFacility));
    assertThat(lineItemDto.getDestination().getName(), is("NGO"));
  }
}