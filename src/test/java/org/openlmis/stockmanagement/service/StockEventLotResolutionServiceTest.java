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
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_CREATION_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_ID_AND_CODE_EXCLUSIVE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_LOT_ORDERABLE_WITHOUT_TRADE_ITEM;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemLotDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class StockEventLotResolutionServiceTest {

  private static final String TRADE_ITEM = "tradeItem";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private LotReferenceDataService lotReferenceDataService;

  @Mock
  private LotCodeValidator lotCodeValidator;

  @InjectMocks
  private StockEventLotResolutionService service;

  private final UUID orderableId = randomUUID();
  private final UUID tradeItemId = randomUUID();

  @Before
  public void setUp() {
    OrderableDto orderable = new OrderableDtoDataBuilder()
        .withId(orderableId)
        .withIdentifier(TRADE_ITEM, tradeItemId.toString())
        .build();
    when(orderableReferenceDataService.findByIds(any())).thenReturn(singletonList(orderable));
  }

  @Test
  public void shouldReuseExistingLotAndBackfillLotIdCaseInsensitively() {
    UUID existingLotId = randomUUID();
    when(lotReferenceDataService.getAllLotsOf(tradeItemId))
        .thenReturn(singletonList(lotDto(existingLotId, "ABC123")));
    StockEventLineItemDto line = receiveLine("abc123");

    service.resolve(event(line));

    assertEquals(existingLotId, line.getLotId());
    verify(lotReferenceDataService, never()).create(any());
  }

  @Test
  public void shouldCreateActiveLotForReceiveWhenAbsent() {
    UUID createdLotId = randomUUID();
    when(lotReferenceDataService.getAllLotsOf(tradeItemId)).thenReturn(emptyList());
    when(lotReferenceDataService.create(any())).thenReturn(lotDto(createdLotId, "NEW1"));
    StockEventLineItemDto line = receiveLine("NEW1");

    service.resolve(event(line));

    assertEquals(createdLotId, line.getLotId());
    ArgumentCaptor<LotDto> captor = ArgumentCaptor.forClass(LotDto.class);
    verify(lotReferenceDataService).create(captor.capture());
    LotDto sent = captor.getValue();
    assertEquals("NEW1", sent.getLotCode());
    assertEquals(tradeItemId, sent.getTradeItemId());
    assertTrue(sent.isActive());
  }

  @Test
  public void shouldCreateLotForPhysicalInventoryWhenAbsent() {
    when(lotReferenceDataService.getAllLotsOf(tradeItemId)).thenReturn(emptyList());
    when(lotReferenceDataService.create(any())).thenReturn(lotDto(randomUUID(), "PI1"));

    service.resolve(event(physicalInventoryLine("PI1")));

    verify(lotReferenceDataService).create(any());
  }

  @Test
  public void shouldRejectCreationForIssueWhenLotAbsent() {
    when(lotReferenceDataService.getAllLotsOf(tradeItemId)).thenReturn(emptyList());
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_LOT_CREATION_NOT_ALLOWED);

    service.resolve(event(issueLine("NOPE")));
  }

  @Test
  public void shouldReuseExistingLotForIssueWithoutCreating() {
    UUID existingLotId = randomUUID();
    when(lotReferenceDataService.getAllLotsOf(tradeItemId))
        .thenReturn(singletonList(lotDto(existingLotId, "EX1")));
    StockEventLineItemDto line = issueLine("EX1");

    service.resolve(event(line));

    assertEquals(existingLotId, line.getLotId());
    verify(lotReferenceDataService, never()).create(any());
  }

  @Test
  public void shouldRejectWhenBothLotIdAndLotPresent() {
    StockEventLineItemDto line = receiveLine("ABC1");
    line.setLotId(randomUUID());
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_LOT_ID_AND_CODE_EXCLUSIVE);

    service.resolve(event(line));
  }

  @Test
  public void shouldPropagateLotCodeValidationError() {
    doThrow(new ValidationMessageException("invalid.lot.code"))
        .when(lotCodeValidator).validate("BAD CODE");
    expectedException.expect(ValidationMessageException.class);

    service.resolve(event(receiveLine("BAD CODE")));
  }

  @Test
  public void shouldRereadAndReuseWhenConcurrentCreateConflicts() {
    UUID existingLotId = randomUUID();
    when(lotReferenceDataService.getAllLotsOf(tradeItemId))
        .thenReturn(emptyList())
        .thenReturn(singletonList(lotDto(existingLotId, "RACE1")));
    when(lotReferenceDataService.create(any()))
        .thenThrow(new ValidationMessageException("some.error"));
    StockEventLineItemDto line = receiveLine("RACE1");

    service.resolve(event(line));

    assertEquals(existingLotId, line.getLotId());
  }

  @Test
  public void shouldIgnoreLineItemsWithoutLotPayload() {
    UUID lotId = randomUUID();
    StockEventLineItemDto line = receiveLine(null);
    line.setLotId(lotId);

    service.resolve(event(line));

    assertEquals(lotId, line.getLotId());
    verify(lotReferenceDataService, never()).getAllLotsOf(any());
    verify(lotReferenceDataService, never()).create(any());
  }

  @Test
  public void shouldRejectWhenOrderableHasNoTradeItem() {
    OrderableDto noTradeItem = new OrderableDtoDataBuilder().withId(orderableId).build();
    when(orderableReferenceDataService.findByIds(any())).thenReturn(singletonList(noTradeItem));
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_LOT_ORDERABLE_WITHOUT_TRADE_ITEM);

    service.resolve(event(receiveLine("ABC1")));
  }

  @Test
  public void shouldFetchLotsOncePerTradeItemAcrossLines() {
    when(lotReferenceDataService.getAllLotsOf(tradeItemId)).thenReturn(emptyList());
    when(lotReferenceDataService.create(any())).thenReturn(lotDto(randomUUID(), "X"));

    StockEventDto event = new StockEventDto();
    event.setLineItems(asList(receiveLine("CODE1"), receiveLine("CODE2")));

    service.resolve(event);

    verify(lotReferenceDataService, times(1)).getAllLotsOf(tradeItemId);
    verify(lotReferenceDataService, times(2)).create(any());
  }

  @Test
  public void shouldResolveCodeLinesAndLeaveLotIdLinesUntouched() {
    UUID existingLotId = randomUUID();
    when(lotReferenceDataService.getAllLotsOf(tradeItemId))
        .thenReturn(singletonList(lotDto(existingLotId, "ABC")));

    StockEventLineItemDto idLine = new StockEventLineItemDto();
    UUID presetLotId = randomUUID();
    idLine.setOrderableId(randomUUID());
    idLine.setLotId(presetLotId);
    idLine.setSourceId(randomUUID());

    StockEventLineItemDto codeLine = receiveLine("ABC");
    StockEventDto event = new StockEventDto();
    event.setLineItems(asList(codeLine, idLine));

    service.resolve(event);

    assertEquals(existingLotId, codeLine.getLotId());
    assertEquals(presetLotId, idLine.getLotId());
    verify(lotReferenceDataService, never()).create(any());
  }

  private StockEventDto event(StockEventLineItemDto line) {
    StockEventDto event = new StockEventDto();
    event.setLineItems(singletonList(line));
    return event;
  }

  private StockEventLineItemLotDto lotPayload(String code) {
    return StockEventLineItemLotDto.builder()
        .lotCode(code)
        .expirationDate(LocalDate.of(2030, 1, 31))
        .build();
  }

  private LotDto lotDto(UUID id, String code) {
    return LotDto.builder().id(id).lotCode(code).tradeItemId(tradeItemId).active(true).build();
  }

  private StockEventLineItemDto baseLine(String code) {
    StockEventLineItemDto line = new StockEventLineItemDto();
    line.setOrderableId(orderableId);
    line.setQuantity(5);
    if (code != null) {
      line.setLot(lotPayload(code));
    }
    return line;
  }

  private StockEventLineItemDto receiveLine(String code) {
    StockEventLineItemDto line = baseLine(code);
    line.setSourceId(randomUUID());
    return line;
  }

  private StockEventLineItemDto issueLine(String code) {
    StockEventLineItemDto line = baseLine(code);
    line.setDestinationId(randomUUID());
    return line;
  }

  private StockEventLineItemDto physicalInventoryLine(String code) {
    return baseLine(code);
  }
}
