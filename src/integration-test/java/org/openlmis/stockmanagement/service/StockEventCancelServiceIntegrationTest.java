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

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventCancelDto;
import org.openlmis.stockmanagement.dto.StockEventCancelLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.CalculatedStockOnHandRepository;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
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
@SuppressWarnings("PMD.TooManyMethods")
public class StockEventCancelServiceIntegrationTest extends BaseIntegrationTest {

  private static final String CANCEL_TAG = "cancel";
  private static final String CANCEL_MOVEMENT_TAG = "cancelMovement";
  private static final LocalDate MOVEMENT_DATE = LocalDate.now().minusDays(45);
  private static final LocalDate MID_HISTORY = LocalDate.now().minusDays(20);
  private static final String PHYSICAL_INVENTORY_DOCUMENT_NUMBER = "PI-IT";

  @MockBean
  StockEventValidationsService stockEventValidationsService;

  @MockBean
  PhysicalInventoryService physicalInventoryService;

  @MockBean
  StockEventNotificationProcessor stockEventNotificationProcessor;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private DocumentNumberGenerator documentNumberGenerator;

  @MockBean
  OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private StockEventCancelService stockEventCancelService;

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Autowired
  private NodeRepository nodeRepository;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Autowired
  private StockCardLineItemRepository stockCardLineItemRepository;

  @Autowired
  private StockEventCancelValidationService cancelValidationService;

  @PersistenceContext
  private EntityManager entityManager;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private OAuth2Authentication authentication;

  private final UUID facilityId = UUID.randomUUID();
  private final UUID programId = UUID.randomUUID();
  private final UUID orderableId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();

  private Node node;
  private StockCardLineItemReason cancelledIssue;
  private StockCardLineItemReason cancelledReceipt;

  @Before
  public void setUp() {
    node = new Node();
    node.setReferenceId(UUID.randomUUID());
    node.setRefDataFacility(false);
    nodeRepository.save(node);

    cancelledIssue = reasonRepository.save(new StockCardLineItemReasonDataBuilder()
        .withoutId().withName("IT cancelled issue").withCreditType().withAdjustmentCategory()
        .withTags(new HashSet<>(asList(CANCEL_TAG, CANCEL_MOVEMENT_TAG))).build());
    cancelledReceipt = reasonRepository.save(new StockCardLineItemReasonDataBuilder()
        .withoutId().withName("IT cancelled receipt").withDebitType().withAdjustmentCategory()
        .withTags(new HashSet<>(asList(CANCEL_TAG, CANCEL_MOVEMENT_TAG))).build());

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(true);
    doNothing().when(permissionService).canCancelStockEvent(programId, facilityId);
    when(documentNumberGenerator.generate(facilityId)).thenReturn("DOC-1");
  }

  @After
  public void tearDown() {
    physicalInventoriesRepository.deleteAll();
    calculatedStockOnHandRepository.deleteAll();
    stockCardRepository.deleteAll();
    stockEventsRepository.deleteAll();
    nodeRepository.deleteAll();
    reasonRepository.delete(cancelledIssue);
    reasonRepository.delete(cancelledReceipt);
  }

  @Test
  public void shouldDateCancellationOnTheMovementItCancelsRatherThanToday() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(100, MOVEMENT_DATE);

    UUID cancellationId = cancel(issueId, cancelledIssue);

    StockEventLineItem cancellation = onlyLineItemOf(cancellationId);
    assertEquals(MOVEMENT_DATE, cancellation.getOccurredDate());
    assertNotEquals(LocalDate.now(), cancellation.getOccurredDate());
  }

  @Test
  public void shouldRecordWhenTheCancellationWasMadeEvenThoughItIsBackdated() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(100, MOVEMENT_DATE);

    UUID cancellationId = cancel(issueId, cancelledIssue);

    ZonedDateTime processed = stockEventsRepository.findById(cancellationId).get()
        .getProcessedDate();
    assertEquals(LocalDate.now(), processed.toLocalDate());
  }

  @Test
  public void shouldLeaveNoStockoutBehindWhenCancellingAnIssueThatEmptiedTheCard() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(100, MOVEMENT_DATE);
    assertEquals(0, stockOnHandAsOf(MID_HISTORY));

    cancel(issueId, cancelledIssue);

    assertEquals(100, stockOnHandAsOf(MID_HISTORY));
    assertEquals(100, stockOnHandAsOf(LocalDate.now()));
  }

  @Test
  public void shouldHealHistoryFromTheReceiveDateWhenCancellingAReceive() {
    receive(200, MOVEMENT_DATE.minusDays(1));
    UUID receiveId = receive(80, MOVEMENT_DATE);
    assertEquals(280, stockOnHandAsOf(MID_HISTORY));

    cancel(receiveId, cancelledReceipt);

    assertEquals(200, stockOnHandAsOf(MID_HISTORY));
    assertEquals(200, stockOnHandAsOf(LocalDate.now()));
  }

  @Test
  public void shouldRefuseCancellationThatWouldTakeStockBelowZeroLaterInHistory() {
    receive(100, MOVEMENT_DATE.minusDays(5));
    UUID receiveId = receive(100, MOVEMENT_DATE);
    issue(150, MOVEMENT_DATE.plusDays(5));
    receive(100, MOVEMENT_DATE.plusDays(10));
    assertEquals(150, stockOnHandAsOf(LocalDate.now()));

    try {
      cancel(receiveId, cancelledReceipt);
      fail("expected the cancellation to be refused");
    } catch (ValidationMessageException expected) {
      assertThat(expected.asMessage().toString(),
          startsWith(ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH));
    }

    assertEquals(150, stockOnHandAsOf(LocalDate.now()));
  }

  @Test
  public void shouldRefuseCancellingMovementWithLaterPhysicalInventory() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(10, MOVEMENT_DATE);
    savePhysicalInventory(MOVEMENT_DATE.plusDays(1), null);

    assertCancellationBlockedByPhysicalInventory(issueId);
  }

  @Test
  public void shouldRefuseCancellingMovementWithSameDayPhysicalInventoryProcessedAfterIt() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(10, MOVEMENT_DATE);
    savePhysicalInventory(MOVEMENT_DATE, processedDateOf(issueId).plusHours(1));

    assertCancellationBlockedByPhysicalInventory(issueId);
  }

  @Test
  public void shouldCancelMovementWithSameDayPhysicalInventoryProcessedBeforeIt() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(10, MOVEMENT_DATE);
    savePhysicalInventory(MOVEMENT_DATE, processedDateOf(issueId).minusHours(1));

    cancel(issueId, cancelledIssue);

    assertEquals(100, stockOnHandAsOf(LocalDate.now()));
  }

  @Test
  public void shouldFindCancellableLineAmongLineItemsLinkedToStockCard() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(10, MOVEMENT_DATE);

    Set<UUID> linked = linkedLineItemIdsOf(issueId);

    assertEquals(singleton(onlyLineItemOf(issueId).getId()), linked);
    assertTrue(cancelValidationService.hasCancellableLine(eventOf(issueId), linked));

    savePhysicalInventory(MOVEMENT_DATE.plusDays(1), null);

    assertFalse(cancelValidationService.hasCancellableLine(eventOf(issueId), linked));
  }

  @Test
  public void shouldFindNoCancellableLineForMovementRecordedBeforeLineItemsWereLinked() {
    receive(100, MOVEMENT_DATE.minusDays(1));
    UUID issueId = issue(10, MOVEMENT_DATE);
    // stock card line items written before the link column existed have no link
    entityManager.flush();
    entityManager.createNativeQuery("UPDATE stockmanagement.stock_card_line_items"
        + " SET origineventlineitemid = NULL WHERE origineventid = CAST(:eventId AS uuid)")
        .setParameter("eventId", issueId.toString())
        .executeUpdate();
    entityManager.clear();

    Set<UUID> linked = linkedLineItemIdsOf(issueId);

    assertTrue(linked.isEmpty());
    assertFalse(cancelValidationService.hasCancellableLine(eventOf(issueId), linked));
  }

  private UUID issue(int quantity, LocalDate occurredDate) {
    return movement(quantity, occurredDate, null, node.getId(), EventOrigin.ISSUE);
  }

  private UUID receive(int quantity, LocalDate occurredDate) {
    return movement(quantity, occurredDate, node.getId(), null, EventOrigin.RECEIVE);
  }

  private UUID movement(int quantity, LocalDate occurredDate, UUID sourceId, UUID destinationId,
      EventOrigin origin) {
    StockEventLineItemDto lineItem = new StockEventLineItemDto();
    lineItem.setOrderableId(orderableId);
    lineItem.setQuantity(quantity);
    lineItem.setOccurredDate(occurredDate);
    lineItem.setSourceId(sourceId);
    lineItem.setDestinationId(destinationId);

    StockEventDto event = new StockEventDto();
    event.setFacilityId(facilityId);
    event.setProgramId(programId);
    event.setUserId(userId);
    event.setActive(true);
    event.setEventOrigin(origin);
    event.setLineItems(singletonList(lineItem));
    setContext(event);

    return stockEventProcessor.process(event);
  }

  private UUID cancel(UUID eventId, StockCardLineItemReason reason) {
    StockEventCancelLineItemDto line = new StockEventCancelLineItemDto();
    line.setStockEventLineItemId(onlyLineItemOf(eventId).getId());
    line.setReasonId(reason.getId());

    StockEventCancelDto request = new StockEventCancelDto();
    request.setSignature("integration-test");
    request.setLineItems(singletonList(line));

    return stockEventCancelService.cancel(eventId, request);
  }

  private void assertCancellationBlockedByPhysicalInventory(UUID eventId) {
    try {
      cancel(eventId, cancelledIssue);
      fail("expected the cancellation to be refused");
    } catch (StockEventCancellationException expected) {
      assertEquals(1, expected.getLineErrors().size());
      StockEventCancellationLineErrorDto lineError = expected.getLineErrors().get(0);
      assertEquals(ERROR_EVENT_LINE_ITEM_BLOCKED_PHYSICAL_INVENTORY, lineError.getMessageKey());
      assertEquals(PHYSICAL_INVENTORY_DOCUMENT_NUMBER,
          lineError.getBlockingTransactions().get(0).getDocumentNumber());
    }
  }

  private Set<UUID> linkedLineItemIdsOf(UUID eventId) {
    return new HashSet<>(
        stockCardLineItemRepository.findOriginEventLineItemIdsByOriginEvent(eventId));
  }

  private StockEvent eventOf(UUID eventId) {
    return stockEventsRepository.findById(eventId).get();
  }

  private ZonedDateTime processedDateOf(UUID eventId) {
    return stockEventsRepository.findById(eventId).get().getProcessedDate();
  }

  // Saved directly: the physical inventory service is mocked here, so processing a physical
  // inventory event would not store one. A null processed date leaves it without a stock event.
  private void savePhysicalInventory(LocalDate occurredDate, ZonedDateTime processedDate) {
    PhysicalInventory inventory = new PhysicalInventory();
    inventory.setProgramId(programId);
    inventory.setFacilityId(facilityId);
    inventory.setIsDraft(false);
    inventory.setOccurredDate(occurredDate);
    inventory.setDocumentNumber(PHYSICAL_INVENTORY_DOCUMENT_NUMBER);
    if (processedDate != null) {
      inventory.setStockEvent(stockEventsRepository.save(new StockEventDataBuilder()
          .withoutId()
          .withFacility(facilityId)
          .withProgram(programId)
          .withProcessedDate(processedDate)
          .build()));
    }

    PhysicalInventoryLineItem lineItem = new PhysicalInventoryLineItem();
    lineItem.setOrderableId(orderableId);
    lineItem.setPhysicalInventory(inventory);
    inventory.setLineItems(singletonList(lineItem));
    physicalInventoriesRepository.save(inventory);
  }

  private StockEventLineItem onlyLineItemOf(UUID eventId) {
    List<StockEventLineItem> lineItems = stockEventsRepository.findById(eventId).get()
        .getLineItems();
    assertEquals(1, lineItems.size());
    return lineItems.get(0);
  }

  private int stockOnHandAsOf(LocalDate date) {
    StockCard card = stockCardRepository
        .findByProgramIdAndFacilityId(programId, facilityId).get(0);
    return calculatedStockOnHandRepository
        .findFirstByStockCardIdAndOccurredDateLessThanEqualOrderByOccurredDateDesc(
            card.getId(), date)
        .map(soh -> soh.getStockOnHand())
        .orElse(0);
  }
}
