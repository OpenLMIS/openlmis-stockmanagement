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

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @MockBean
  private FacilityReferenceDataService facilityReferenceDataService;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @MockBean
  private LotReferenceDataService lotReferenceDataService;

  @MockBean
  private PermissionService permissionService;

  @After
  public void tearDown() throws Exception {
    physicalInventoriesRepository.deleteAll();
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
  }

  @Test
  public void should_save_stock_card_line_items_and_create_stock_card_for_first_movement()
      throws Exception {
    //given
    UUID userId = randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    StockEvent savedEvent = save(stockEventDto, userId);

    //when
    stockCardService.saveFromEvent(stockEventDto, savedEvent.getId());

    //then
    StockCard savedCard = stockCardRepository.findByOriginEvent(savedEvent);
    StockCardLineItem firstLineItem = savedCard.getLineItems().get(0);

    assertThat(firstLineItem.getUserId(), is(userId));
    assertThat(firstLineItem.getSource().isRefDataFacility(), is(true));
    assertThat(firstLineItem.getDestination().isRefDataFacility(), is(false));

    assertThat(firstLineItem.getStockCard().getOriginEvent().getId(), is(savedEvent.getId()));
    assertThat(firstLineItem.getStockCard().getFacilityId(), is(savedEvent.getFacilityId()));
    assertThat(firstLineItem.getStockCard().getProgramId(), is(savedEvent.getProgramId()));
    UUID orderableId = savedEvent.getLineItems().get(0).getOrderableId();
    assertThat(firstLineItem.getStockCard().getOrderableId(), is(orderableId));
  }

  @Test
  public void should_save_line_items_with_program_facility_orderable_for_non_first_movement()
      throws Exception {
    //given
    //1. there is an existing event that caused a stock card to exist
    StockEventDto existingEventDto = createStockEventDto();
    final StockEvent existingEvent = save(existingEventDto, randomUUID());
    UUID orderableId = existingEventDto.getLineItems().get(0).getOrderableId();

    //2. and there is a new event coming
    StockEventDto newEventDto = createStockEventDto();
    newEventDto.setProgramId(existingEventDto.getProgramId());
    newEventDto.setFacilityId(existingEventDto.getFacilityId());
    newEventDto.getLineItems().get(0).setOrderableId(orderableId);

    //when
    long cardAmountBeforeSave = stockCardRepository.count();
    UUID userId = randomUUID();
    StockEvent savedNewEvent = save(newEventDto, userId);
    long cardAmountAfterSave = stockCardRepository.count();

    //then
    StockCard savedCard = stockCardRepository.findByOriginEvent(existingEvent);
    List<StockCardLineItem> lineItems = savedCard.getLineItems();
    StockCardLineItem latestLineItem = lineItems.get(lineItems.size() - 1);

    assertThat(cardAmountAfterSave, is(cardAmountBeforeSave));
    assertThat(latestLineItem.getOriginEvent().getId(), is(savedNewEvent.getId()));
    assertThat(latestLineItem.getStockCard().getId(), is(savedCard.getId()));
    assertThat(latestLineItem.getUserId(), is(userId));
  }

  @Test
  public void should_get_refdata_and_convert_organizations_when_find_stock_card()
      throws Exception {
    //given
    UUID userId = randomUUID();
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setLotId(randomUUID());

    //1. mock ref data service
    FacilityDto cardFacility = new FacilityDto();
    FacilityDto sourceFacility = new FacilityDto();
    ProgramDto programDto = new ProgramDto();
    OrderableDto orderableDto = new OrderableDto();
    LotDto lotDto = new LotDto();

    when(facilityReferenceDataService.findOne(stockEventDto.getFacilityId()))
        .thenReturn(cardFacility);
    when(facilityReferenceDataService.findOne(fromString("e6799d64-d10d-4011-b8c2-0e4d4a3f65ce")))
        .thenReturn(sourceFacility);

    when(programReferenceDataService.findOne(stockEventDto.getProgramId()))
        .thenReturn(programDto);
    when(orderableReferenceDataService
        .findOne(stockEventDto.getLineItems().get(0).getOrderableId()))
        .thenReturn(orderableDto);
    when(lotReferenceDataService
        .findOne(stockEventDto.getLineItems().get(0).getLotId()))
        .thenReturn(lotDto);

    //2. there is an existing stock card with line items
    StockEvent savedEvent = save(stockEventDto, userId);

    //when
    StockCard savedCard = stockCardRepository.findByOriginEvent(savedEvent);
    StockCardDto foundCardDto = stockCardService.findStockCardById(savedCard.getId());

    //then
    assertThat(foundCardDto.getFacility(), is(cardFacility));
    assertThat(foundCardDto.getProgram(), is(programDto));
    assertThat(foundCardDto.getOrderable(), is(orderableDto));
    assertThat(foundCardDto.getLot(), is(lotDto));

    StockCardLineItemDto lineItemDto = foundCardDto.getLineItems().get(0);
    assertThat(lineItemDto.getSource(), is(sourceFacility));
    assertThat(lineItemDto.getDestination().getName(), is("NGO"));
  }

  @Test
  public void should_get_stock_card_with_calculated_soh_when_find_stock_card() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    StockEvent savedEvent = save(stockEventDto, randomUUID());

    //when
    UUID cardId = stockCardRepository.findByOriginEvent(savedEvent).getId();
    StockCardDto card = stockCardService.findStockCardById(cardId);

    //then
    assertThat(card.getStockOnHand(), is(stockEventDto.getLineItems().get(0).getQuantity()));
  }

  @Test
  public void should_reassign_physical_inventory_reason_names() throws Exception {
    //given
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    stockEventDto.getLineItems().get(0).setReasonId(null);
    StockEvent savedEvent = save(stockEventDto, randomUUID());

    //when
    UUID cardId = stockCardRepository.findByOriginEvent(savedEvent).getId();
    StockCardDto card = stockCardService.findStockCardById(cardId);

    //then
    String reasonName = card.getLineItems().get(0).getLineItem().getReason().getName();
    assertThat(reasonName, is("Overstock"));
  }

  @Test
  public void should_return_null_when_can_not_find_stock_card_by_id() throws Exception {
    //when
    UUID nonExistingCardId = randomUUID();
    StockCardDto cardDto = stockCardService.findStockCardById(nonExistingCardId);

    //then
    assertNull(cardDto);
  }

  @Test(expected = PermissionMessageException.class)
  public void should_throw_permission_exception_if_user_has_no_permission_to_view_card()
      throws Exception {
    //given
    StockEvent savedEvent = save(createStockEventDto(), randomUUID());
    doThrow(new PermissionMessageException(new Message("some error")))
        .when(permissionService)
        .canViewStockCard(savedEvent.getProgramId(), savedEvent.getFacilityId());

    //when
    UUID savedCardId = stockCardRepository.findByOriginEvent(savedEvent).getId();
    stockCardService.findStockCardById(savedCardId);
  }

  private StockEvent save(StockEventDto eventDto, UUID userId) {
    StockEventProcessContext context = new StockEventProcessContext();
    context.setCurrentUserId(userId);

    eventDto.setContext(context);

    StockEvent event = eventDto.toEvent();
    StockEvent savedEvent = stockEventsRepository.save(event);
    stockCardService.saveFromEvent(eventDto, savedEvent.getId());
    return savedEvent;
  }
}