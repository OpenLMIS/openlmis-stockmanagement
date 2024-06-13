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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.Organization;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class StockEventProcessorIntegrationTest extends BaseIntegrationTest {

  @MockBean
  StockEventNotificationProcessor stockEventNotificationProcessor;
  @MockBean
  private StockEventValidationsService stockEventValidationsService;
  @MockBean
  private PhysicalInventoryService physicalInventoryService;
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

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private NodeRepository nodeRepository;

  @Autowired
  private StockCardLineItemReasonRepository stockCardLineItemReasonRepository;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private OAuth2Authentication authentication;

  private UUID userId = UUID.randomUUID();

  private long cardSize;
  private long eventSize;
  private long lineItemSize;

  private Node node;

  private StockCardLineItemReason reason;

  @Before
  public void setUp() throws Exception {
    cardSize = stockCardRepository.count();
    eventSize = stockEventsRepository.count();
    lineItemSize = lineItemRepository.count();

    Organization organization = new Organization();
    organization.setName("org");
    organizationRepository.save(organization);

    node = new Node();
    node.setReferenceId(UUID.randomUUID());
    node.setRefDataFacility(false);
    nodeRepository.save(node);

    reason = new StockCardLineItemReason("reason", null, ReasonType.CREDIT,
        ReasonCategory.ADJUSTMENT, false, Collections.emptyList());
    stockCardLineItemReasonRepository.save(reason);

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(true);
  }

  @After
  public void tearDown() throws Exception {
    physicalInventoriesRepository.deleteAll();
    calculatedStockOnHandRepository.deleteAll();
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
    nodeRepository.deleteAll();
    stockCardLineItemReasonRepository.deleteAll();
  }

  @Test
  public void shouldNotSaveEventsIfAnythingGoesWrongInValidationsService()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(reason.getId());
    stockEventDto.getLineItems().get(0).setSourceId(node.getId());
    stockEventDto.getLineItems().get(0).setDestinationId(node.getId());
    stockEventDto.setUserId(userId);
    stockEventDto.setActive(true);
    setContext(stockEventDto);

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
  public void shouldSaveEventAndLineItemsAndCallNotificationsWhenValidationServicePasses()
      throws Exception {
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.getLineItems().get(0).setReasonId(reason.getId());
    stockEventDto.getLineItems().get(0).setSourceId(node.getId());
    stockEventDto.getLineItems().get(0).setDestinationId(node.getId());
    stockEventDto.setUserId(userId);
    stockEventDto.setActive(true);
    setContext(stockEventDto);

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    assertSize(cardSize + 1, eventSize + 1, lineItemSize + 1);
    verify(stockEventNotificationProcessor).callAllNotifications(stockEventDto);
  }

  @Test
  public void shouldSubmitPhysicalInventoryWhenEventIsAboutPhysicalInventory()
      throws Exception {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setUserId(userId);
    stockEventDto.setActive(true);
    stockEventDto.getLineItems().get(0).setReasonId(null);
    stockEventDto.getLineItems().get(0).setSourceId(null);
    stockEventDto.getLineItems().get(0).setDestinationId(null);
    setContext(stockEventDto);

    //when
    stockEventProcessor.process(stockEventDto);

    //then
    verify(physicalInventoryService)
        .submitPhysicalInventory(any(PhysicalInventoryDto.class), any(UUID.class));
  }

  @Test
  public void shouldSaveUnitOfOrderableIdOnProcessingStockEvent() {
    //given
    StockEventDto stockEventDto = createStockEventDto();
    stockEventDto.setUserId(userId);
    stockEventDto.setActive(true);
    StockEventLineItemDto eventLineItemDto = stockEventDto.getLineItems().get(0);
    eventLineItemDto.setReasonId(null);
    eventLineItemDto.setSourceId(null);
    eventLineItemDto.setDestinationId(null);
    UUID unitOfOrderableId = eventLineItemDto.getUnitOfOrderableId();

    //when
    UUID eventId = stockEventProcessor.process(stockEventDto);

    //then
    Optional<StockEvent> stockEventOptional = stockEventsRepository.findById(eventId);
    assertThat(stockEventOptional.isPresent(), is(true));
    StockEvent stockEvent = stockEventOptional.get();
    List<StockEventLineItem> lineItems = stockEvent.getLineItems();
    assertThat(lineItems, hasSize(1));
    assertThat(lineItems.get(0).getUnitOfOrderableId(),
        is(unitOfOrderableId));

    StockCard card = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableIdAndLotId(
        stockEventDto.getProgramId(),
        stockEventDto.getFacilityId(),
        eventLineItemDto.getOrderableId(),
        eventLineItemDto.getLotId()
    );
    assertThat(card, isNotNull());
    assertThat(card.getUnitOfOrderableId(), is(unitOfOrderableId));
  }

  private void assertSize(long cardSize, long eventSize, long lineItemSize) {
    assertThat(stockCardRepository.count(), is(cardSize));
    assertThat(stockEventsRepository.count(), is(eventSize));
    assertThat(lineItemRepository.count(), is(lineItemSize));
  }
}
