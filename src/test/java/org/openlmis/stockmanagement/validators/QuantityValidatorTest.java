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

package org.openlmis.stockmanagement.validators;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID;
import static org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder.createStockEventDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventAdjustmentDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventLineItemDtoDataBuilder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class QuantityValidatorTest extends BaseValidatorTest {

  @Rule
  public ExpectedException expectedException = none();

  @InjectMocks
  private QuantityValidator quantityValidator;

  private StockCardLineItemReason creditAdjustmentReason;
  private StockCardLineItemReason debitAdjustmentReason;
  private LocalDate firstDate = LocalDate.of(2015, 1, 1);

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    creditAdjustmentReason = new StockCardLineItemReasonDataBuilder()
        .withAdjustmentCategory()
        .build();

    debitAdjustmentReason = new StockCardLineItemReasonDataBuilder()
        .withAdjustmentCategory()
        .withDebitType()
        .build();
  }

  @Test
  public void shouldNotRejectWhenEventHasNoDestinationOrDebitReason() {
    StockEventDto stockEventDto = new StockEventDto();

    quantityValidator.validate(stockEventDto);
  }

  @Test
  public void shouldNotRejectWhenEventReasonIdIsNotFound() {
    StockEventDto event = createStockEventDto();

    StockEventLineItemDto invalidItem = event.getLineItems().get(0);
    invalidItem.setDestinationId(null);

    setContext(event);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenEventLineItemHasNoReason() {
    StockEventDto event = createStockEventDto();
    setContext(event);

    StockEventLineItemDto invalidItem = event.getLineItems().get(0);
    invalidItem.setDestinationId(randomUUID());
    invalidItem.setReasonId(null);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandMatchesQuantityAndNoAdjustments() {
    StockEventDto event = createPhysicalInventoryEventDto(firstDate.plusDays(1), 0, null);
    setContext(event);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandWithAdjustmentsMatchesQuantity() {
    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 10);

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(lineItem));

    StockEventDto event = spy(createPhysicalInventoryEventDto(firstDate.plusDays(2), 5,
        singletonList(createDebitAdjustment(5))));
    mockCardFound(event, card);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldRejectWhenAnyAdjustmentHasNegativeQuantity() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID);

    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 15);

    StockCard card = new StockCard();
    card.setLineItems(singletonList(lineItem));

    StockEventDto event = spy(createPhysicalInventoryEventDto(firstDate.plusDays(2), 5,
        singletonList(createCreditAdjustment(-2))));
    mockCardFound(event, card);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandDoesNotMatchQuantityAndNoAdjustmentsProvided() {
    StockCardLineItem lineItem = createCreditLineItem(firstDate.plusDays(1), 15);

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(lineItem));

    StockEventDto event = createPhysicalInventoryEventDto(firstDate.plusDays(2), 5, null);
    mockCardFound(event, card);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandWithAdjustmentsDoesNotMatchQuantity() {
    StockCardLineItem lineItem = createCreditLineItem(
        firstDate.plusDays(1), 15);

    StockCard card = new StockCard();
    card.setLineItems(newArrayList(lineItem));

    StockEventDto event = spy(createPhysicalInventoryEventDto(firstDate.plusDays(2), 5,
        singletonList(createCreditAdjustment(5))));
    mockCardFound(event, card);

    quantityValidator.validate(event);
  }

  @Test
  public void shouldNotRejectWhenStockOnHandFromMiddleIsLessThanStockAdjustment() {
    StockCard card = new StockCardDataBuilder(new StockEventDataBuilder().build())
        .withLineItem(buildPhysicalInventoryLineItem(100, Month.JANUARY))
        .withLineItem(buildPhysicalInventoryLineItem(80, Month.MARCH))
        .build();

    StockEventDto event =
        createPhysicalInventoryEventDto(LocalDate.of(2017, Month.FEBRUARY, 28), 15, null);
    mockCardFound(event, card);

    quantityValidator.validate(event);
  }

  private StockCardLineItem buildPhysicalInventoryLineItem(int quantity, Month month) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(LocalDate.of(2017, month, 31))
        .processedDate(getProcessedDateForLocalDate(LocalDate.now().minusDays(1)))
        .build();
  }

  private ZonedDateTime getProcessedDateForLocalDate(LocalDate date) {
    return ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC);
  }

  private StockEventDto createDebitEventDto(LocalDate date, int quantity) {
    return new StockEventDtoDataBuilder()
        .addLineItem(new StockEventLineItemDtoDataBuilder()
            .withReasonId(debitAdjustmentReason.getId())
            .withQuantity(quantity)
            .withOccurredDate(date)
            .buildForAdjustment())
        .build();
  }

  private StockEventDto createPhysicalInventoryEventDto(LocalDate date, int quantity,
                                                        List<StockEventAdjustmentDto> adjustments) {

    return new StockEventDtoDataBuilder()
        .addLineItem(new StockEventLineItemDtoDataBuilder()
            .addStockAdjustments(adjustments)
            .withQuantity(quantity)
            .withOccurredDate(date)
            .buildForPhysicalInventory())
        .build();
  }

  private StockCardLineItem createDebitLineItem(LocalDate date, int quantity) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .reason(debitAdjustmentReason)
        .build();
  }

  private StockCardLineItem createCreditLineItem(LocalDate date, int quantity) {
    return StockCardLineItem
        .builder()
        .quantity(quantity)
        .occurredDate(date)
        .processedDate(ZonedDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC))
        .reason(creditAdjustmentReason)
        .build();
  }

  private StockEventAdjustmentDto createCreditAdjustment(int quantity) {
    return new StockEventAdjustmentDto(creditAdjustmentReason.getId(), quantity);
  }

  private StockEventAdjustmentDto createDebitAdjustment(int quantity) {
    return new StockEventAdjustmentDto(debitAdjustmentReason.getId(), quantity);
  }

  private void mockCardFound(StockEventDto event, StockCard card) {
    card.setOrderableId(event.getLineItems().get(0).getOrderableId());
    card.setLotId(event.getLineItems().get(0).getLotId());

    lenient().when(calculatedStockOnHandService
        .getStockCardsWithStockOnHand(event.getProgramId(), event.getFacilityId()))
        .thenReturn(singletonList(card));

    setContext(event);
    setReasons(event, newArrayList(creditAdjustmentReason, debitAdjustmentReason));
  }
}
